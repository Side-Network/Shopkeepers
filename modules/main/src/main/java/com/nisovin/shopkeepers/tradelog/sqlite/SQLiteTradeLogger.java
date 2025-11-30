package com.nisovin.shopkeepers.tradelog.sqlite;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.tradelog.TradeLogStorageType;
import com.nisovin.shopkeepers.tradelog.TradeLogUtils;
import com.nisovin.shopkeepers.tradelog.base.AbstractFileTradeLogger;
import com.nisovin.shopkeepers.tradelog.data.PlayerRecord;
import com.nisovin.shopkeepers.tradelog.data.ShopRecord;
import com.nisovin.shopkeepers.tradelog.data.TradeRecord;
import com.nisovin.shopkeepers.tradelog.history.PlayerSelector;
import com.nisovin.shopkeepers.tradelog.history.ShopSelector;
import com.nisovin.shopkeepers.tradelog.history.TradingHistoryProvider;
import com.nisovin.shopkeepers.tradelog.history.TradingHistoryRequest;
import com.nisovin.shopkeepers.tradelog.history.TradingHistoryResult;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;
import com.nisovin.shopkeepers.util.java.FileUtils;
import com.nisovin.shopkeepers.util.java.JdbcUtils;
import com.nisovin.shopkeepers.util.java.Range;
import com.nisovin.shopkeepers.util.java.Retry;
import com.nisovin.shopkeepers.util.java.StringUtils;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Logs trades to an SQLite database.
 */
public class SQLiteTradeLogger extends AbstractFileTradeLogger implements TradingHistoryProvider {

	private static final int TRANSACTION_RETRY_MAX_ATTEMPTS = 20;
	private static final long TRANSACTION_RETRY_DELAY_MILLIS = 50L;

	private static final String FILE_NAME = "trades.db";
	private static final String TABLE_TRADE = "trade";
	private static final String COLUMN_TIMESTAMP = "timestamp";
	private static final String COLUMN_PLAYER_UUID = "player_uuid";
	private static final String COLUMN_PLAYER_NAME = "player_name";
	private static final String COLUMN_SHOP_UUID = "shop_uuid";
	private static final String COLUMN_SHOP_TYPE = "shop_type";
	private static final String COLUMN_SHOP_WORLD = "shop_world";
	private static final String COLUMN_SHOP_X = "shop_x";
	private static final String COLUMN_SHOP_Y = "shop_y";
	private static final String COLUMN_SHOP_Z = "shop_z";
	private static final String COLUMN_SHOP_OWNER_UUID = "shop_owner_uuid";
	private static final String COLUMN_SHOP_OWNER_NAME = "shop_owner_name";
	private static final String COLUMN_ITEM_1_TYPE = "item_1_type";
	private static final String COLUMN_ITEM_1_AMOUNT = "item_1_amount";
	private static final String COLUMN_ITEM_1_METADATA = "item_1_metadata";
	private static final String COLUMN_ITEM_2_TYPE = "item_2_type";
	private static final String COLUMN_ITEM_2_AMOUNT = "item_2_amount";
	private static final String COLUMN_ITEM_2_METADATA = "item_2_metadata";
	private static final String COLUMN_RESULT_ITEM_TYPE = "result_item_type";
	private static final String COLUMN_RESULT_ITEM_AMOUNT = "result_item_amount";
	private static final String COLUMN_RESULT_ITEM_METADATA = "result_item_metadata";
	private static final String COLUMN_TRADE_COUNT = "trade_count";

	// Note: SQLite does not have rigid data types, but storage classes and type affinity. The data
	// types specified here are not enforced by SQLite or us, but only used to document the expected
	// structure of the data.
	private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_TRADE + " ("
	// ISO 8601 UTC date time with fractional seconds:
			+ COLUMN_TIMESTAMP + " VARCHAR(30) NOT NULL, "
			+ COLUMN_PLAYER_UUID + " CHARACTER(36) NOT NULL, "
			+ COLUMN_PLAYER_NAME + " VARCHAR(16) NOT NULL, "
			+ COLUMN_SHOP_UUID + " CHARACTER(36) NOT NULL, "
			+ COLUMN_SHOP_TYPE + " VARCHAR(32) NOT NULL, "
			+ COLUMN_SHOP_WORLD + " VARCHAR(32), " // Null for virtual shops
			+ COLUMN_SHOP_X + " INTEGER NOT NULL, " // 0 for virtual shops
			+ COLUMN_SHOP_Y + " INTEGER NOT NULL, "
			+ COLUMN_SHOP_Z + " INTEGER NOT NULL, "
			+ COLUMN_SHOP_OWNER_UUID + " CHARACTER(36), " // Shop owner, null for admin shops
			+ COLUMN_SHOP_OWNER_NAME + " VARCHAR(16), "
			+ COLUMN_ITEM_1_TYPE + " VARCHAR(64) NOT NULL, "
			+ COLUMN_ITEM_1_AMOUNT + " TINYINT UNSIGNED NOT NULL, "
			+ COLUMN_ITEM_1_METADATA + " TEXT NOT NULL, " // Empty if the item has no metadata
			+ COLUMN_ITEM_2_TYPE + " VARCHAR(64), " // Second item is optional and can thus be null
			+ COLUMN_ITEM_2_AMOUNT + " TINYINT UNSIGNED, "
			+ COLUMN_ITEM_2_METADATA + " TEXT, "
			+ COLUMN_RESULT_ITEM_TYPE + " VARCHAR(64) NOT NULL, "
			+ COLUMN_RESULT_ITEM_AMOUNT + " TINYINT UNSIGNED NOT NULL, "
			+ COLUMN_RESULT_ITEM_METADATA + " TEXT NOT NULL, "
			+ COLUMN_TRADE_COUNT + " SMALLINT UNSIGNED NOT NULL"
			+ ");";
	private static final String INSERT_TRADE_SQL = "INSERT INTO " + TABLE_TRADE
			+ "(" + String.join(", ",
					COLUMN_TIMESTAMP,
					COLUMN_PLAYER_UUID,
					COLUMN_PLAYER_NAME,
					COLUMN_SHOP_UUID,
					COLUMN_SHOP_TYPE,
					COLUMN_SHOP_WORLD,
					COLUMN_SHOP_X,
					COLUMN_SHOP_Y,
					COLUMN_SHOP_Z,
					COLUMN_SHOP_OWNER_UUID,
					COLUMN_SHOP_OWNER_NAME,
					COLUMN_ITEM_1_TYPE,
					COLUMN_ITEM_1_AMOUNT,
					COLUMN_ITEM_1_METADATA,
					COLUMN_ITEM_2_TYPE,
					COLUMN_ITEM_2_AMOUNT,
					COLUMN_ITEM_2_METADATA,
					COLUMN_RESULT_ITEM_TYPE,
					COLUMN_RESULT_ITEM_AMOUNT,
					COLUMN_RESULT_ITEM_METADATA,
					COLUMN_TRADE_COUNT)
			+ ") "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static String getSelectTradesSql(
			boolean filterByPlayer,
			boolean filterByShop,
			boolean filterByOwner,
			boolean filterByAdminShop,
			boolean filterByPlayerShop
	) {
		var query = "SELECT * FROM " + TABLE_TRADE;

		var filters = new ArrayList<String>();
		if (filterByPlayer) {
			filters.add(COLUMN_PLAYER_UUID + "=?");
		}
		if (filterByShop) {
			filters.add(COLUMN_SHOP_UUID + "=?");
		}
		if (filterByOwner) {
			filters.add(COLUMN_SHOP_OWNER_UUID + "=?");
		}
		// Assumption: All player shops always have an owner and all admin shops always have no
		// owner.
		if (filterByAdminShop) {
			filters.add(COLUMN_SHOP_OWNER_UUID + " IS NULL");
		}
		if (filterByPlayerShop) {
			filters.add(COLUMN_SHOP_OWNER_UUID + " IS NOT NULL");
		}

		if (!filters.isEmpty()) {
			query += " WHERE " + String.join(" AND ", filters);
		}

		query += " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT ? OFFSET ?;";
		return query;
	}

	private static String toTradesCountSql(String selectTradesSql) {
		String tradesCountSql = selectTradesSql;
		tradesCountSql = StringUtils.replaceFirst(tradesCountSql, "SELECT *", "SELECT COUNT(*)");
		tradesCountSql = StringUtils.replaceFirst(tradesCountSql, " ORDER BY " + COLUMN_TIMESTAMP + " DESC", "");
		tradesCountSql = StringUtils.replaceFirst(tradesCountSql, " LIMIT ? OFFSET ?", "");
		return tradesCountSql;
	}

	private final String connectionURL;

	private volatile @Nullable String setupFailureReason = null;
	private volatile boolean performSetupAgain = false;

	public SQLiteTradeLogger(SKShopkeepersPlugin plugin) {
		super(plugin, TradeLogStorageType.SQLITE);

		this.connectionURL = "jdbc:sqlite:" + tradeLogsFolder.resolve(FILE_NAME);
	}

	// TODO Keep the connection open (at least until we observe an error)? Cache the
	// PreparedStatements?
	private Connection getConnection() throws SQLException, IOException {
		FileUtils.createDirectories(tradeLogsFolder);
		return DriverManager.getConnection(connectionURL);
	}

	@FunctionalInterface
	public interface SqlTransaction<T> {

		public T execute(Connection connection) throws Exception;
	}

	/**
	 * Runs the given transaction.
	 * 
	 * @param <T>
	 *            The return type.
	 * @param transaction
	 *            The transaction logic.
	 * @return The result of the transaction.
	 * @throws Exception
	 *             If something goes wrong.
	 */
	private <T> T runTransaction(SqlTransaction<T> transaction) throws Exception {
		boolean done = false;
		@Nullable T result = null;
		try (var connection = this.getConnection()) {
			if (performSetupAgain) {
				this.performSetup(connection);
			}

			result = transaction.execute(connection);

			// We are about to close the connection:
			done = true;
			return Unsafe.cast(result);
		} catch (Exception e) {
			if (done) {
				// The transaction completed successfully: We log but otherwise ignore any
				// exceptions raised during the closing of the connection, so that they do not
				// trigger a retry of the transaction.
				Log.severe("Failed to close the database connection!", e);
				return Unsafe.cast(result);
			}

			// If the transaction failed, we re-attempt the database setup during the subsequent
			// retry to handle cases in which the database file might have been dynamically deleted.
			performSetupAgain = true;
			throw e;
		}
	}

	private <T> T retryTransaction(SqlTransaction<T> transaction) throws Exception {
		return Retry.retry(() -> {
			return this.runTransaction(transaction);
		}, TRANSACTION_RETRY_MAX_ATTEMPTS, (attemptNumber, exception, retry) -> {
			// Try again after a small delay:
			if (retry) {
				try {
					Thread.sleep(TRANSACTION_RETRY_DELAY_MILLIS);
				} catch (InterruptedException e) {
					// Restore the interrupt status for anyone interested in it, but otherwise
					// ignore the interrupt here:
					Thread.currentThread().interrupt();
				}
			}
		});
	}

	@Override
	protected void asyncSetup() {
		super.asyncSetup();

		try (Connection connection = this.getConnection()) {
			this.performSetup(connection);
		} catch (Exception e) {
			setupFailureReason = e.getMessage();
			Log.severe(logPrefix + setupFailureReason, e);
		}
	}

	@Override
	protected void postSetup() {
		var setupFailureReason = this.setupFailureReason;
		if (setupFailureReason != null) {
			this.disable(setupFailureReason);
		}
	}

	private void performSetup(Connection connection) throws Exception {
		this.createTable(connection);
	}

	private void createTable(Connection connection) throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute(CREATE_TABLE_SQL);
		} catch (Exception e) {
			throw new Exception("Could not create table '" + TABLE_TRADE + "'!", e);
		}
	}

	@Override
	protected void writeTrades(SaveContext saveContext) throws Exception {
		if (!saveContext.hasUnsavedTrades()) {
			return;
		}

		// Note: Retrying is handled by the caller.
		this.runTransaction(connection -> {
			boolean done = false;
			try (var insertStatement = connection.prepareStatement(INSERT_TRADE_SQL)) {
				var trade = saveContext.getNextUnsavedTrade();
				while (trade != null) {
					this.insertTrade(insertStatement, trade);

					// Trade successfully saved:
					saveContext.onTradeSuccessfullySaved();

					// Get the next trade to insert:
					trade = saveContext.getNextUnsavedTrade();
				}

				return null; // No result to return
			} catch (Exception e) {
				if (done) {
					// The saving completed successfully: We log but otherwise ignore any exceptions
					// raised during the closing of the statement, so that they do not trigger a
					// retry of the saving.
					Log.severe("Failed to close the database statement", e);
					return null; // No result to return
				}

				throw e;
			}
		});
	}

	private void insertTrade(PreparedStatement insertStatement, TradeRecord trade)
			throws SQLException {
		Instant timestamp = trade.getTimestamp();
		PlayerRecord player = trade.getPlayer();

		ShopRecord shop = trade.getShop();
		PlayerRecord shopOwner = shop.getOwner();
		@Nullable String shopOwnerId = null;
		@Nullable String shopOwnerName = null;
		if (shopOwner != null) {
			shopOwnerId = shopOwner.getUniqueId().toString();
			shopOwnerName = shopOwner.getName();
		}

		UnmodifiableItemStack resultItem = trade.getResultItem();
		UnmodifiableItemStack item1 = trade.getItem1();
		UnmodifiableItemStack item2 = trade.getItem2(); // Can be null
		@Nullable String item2Type = null;
		@Nullable Integer item2Amount = null;
		@Nullable String item2Metadata = null;
		if (item2 != null) {
			item2Type = item2.getType().name(); // TODO Store the namespaced key instead
			item2Amount = item2.getAmount();
			item2Metadata = this.getItemMetadata(item2);
		}

		insertStatement.setString(1, timestamp.toString()); // timestamp as ISO UTC

		insertStatement.setString(2, player.getUniqueId().toString()); // player_uuid
		insertStatement.setString(3, player.getName()); // player_name

		insertStatement.setString(4, shop.getUniqueId().toString()); // shop_uuid
		insertStatement.setString(5, shop.getTypeId()); // shop_type
		insertStatement.setString(6, shop.getWorldName()); // shop_world
		insertStatement.setInt(7, shop.getX()); // shop_x
		insertStatement.setInt(8, shop.getY()); // shop_y
		insertStatement.setInt(9, shop.getZ()); // shop_z

		insertStatement.setString(10, shopOwnerId); // shop_owner_uuid
		insertStatement.setString(11, shopOwnerName); // shop_owner_name

		// TODO Store the namespaced key instead
		insertStatement.setString(12, item1.getType().name()); // item_1_type
		insertStatement.setInt(13, item1.getAmount()); // item_1_amount
		insertStatement.setString(14, this.getItemMetadata(item1)); // item_1_metadata

		insertStatement.setString(15, item2Type); // item_2_type
		insertStatement.setObject(16, item2Amount, Types.TINYINT); // item_2_amount
		insertStatement.setString(17, item2Metadata); // item_2_metadata

		// TODO Store the namespaced key instead
		insertStatement.setString(18, resultItem.getType().name()); // result_item_type
		insertStatement.setInt(19, resultItem.getAmount()); // result_item_amount
		insertStatement.setString(20, this.getItemMetadata(resultItem)); // result_item_metadata

		insertStatement.setInt(21, trade.getTradeCount()); // trade_count

		insertStatement.executeUpdate();
	}

	@Override
	public CompletableFuture<TradingHistoryResult> getTradingHistory(TradingHistoryRequest request) {
		return CompletableFuture.supplyAsync(() -> {
			PlayerSelector playerSelector = request.playerSelector;
			ShopSelector shopSelector = request.shopSelector;
			Range range = request.range;

			boolean filterByPlayer;
			boolean filterByShop;
			boolean filterByOwner;
			boolean filterByAdminShop;
			boolean filterByPlayerShop;

			var filterParameters = new ArrayList<Object>();

			if (playerSelector == PlayerSelector.ALL) {
				filterByPlayer = false;
			} else if (playerSelector instanceof PlayerSelector.ByUUID playerByUUIDSelector) {
				filterByPlayer = true;

				filterParameters.add(playerByUUIDSelector.getPlayerUUID().toString());
			} else {
				throw Validate.State.error("Unexpected player selector: "
						+ playerSelector.getClass().getName());
			}

			if (shopSelector == ShopSelector.ALL) {
				filterByShop = false;
				filterByOwner = false;
				filterByAdminShop = false;
				filterByPlayerShop = false;
			} else if (shopSelector == ShopSelector.ADMIN_SHOPS) {
				filterByShop = false;
				filterByOwner = false;
				filterByAdminShop = true;
				filterByPlayerShop = false;
			} else if (shopSelector == ShopSelector.PLAYER_SHOPS) {
				filterByShop = false;
				filterByOwner = false;
				filterByAdminShop = false;
				filterByPlayerShop = true;
			} else if (shopSelector instanceof ShopSelector.ByOwnerUUID byOwnerUUIDSelector) {
				filterByShop = false;
				filterByOwner = true;
				filterByAdminShop = false;
				filterByPlayerShop = false;

				filterParameters.add(byOwnerUUIDSelector.getOwnerUUID().toString());
			} else if (shopSelector instanceof ShopSelector.ByShopUUID byShopUUIDSelector) {
				filterByShop = true;
				filterByOwner = false;
				filterByAdminShop = false;
				filterByPlayerShop = false;

				filterParameters.add(byShopUUIDSelector.getShopUUID().toString());

				var ownerUUID = byShopUUIDSelector.getOwnerUUID();
				if (ownerUUID != null) {
					filterByOwner = true;
					filterParameters.add(ownerUUID.toString());
				}
			} else {
				throw Validate.State.error("Unexpected shop selector: "
						+ shopSelector.getClass().getName());
			}

			var selectTradesSql = getSelectTradesSql(
					filterByPlayer,
					filterByShop,
					filterByOwner,
					filterByAdminShop,
					filterByPlayerShop
			);
			var tradesCountSql = toTradesCountSql(selectTradesSql);

			try {
				return this.retryTransaction(connection -> {
					int totalTradesCount = 0;
					List<TradeRecord> trades = new ArrayList<>();

					try (var tradesCountStatement = connection.prepareStatement(tradesCountSql)) {
						JdbcUtils.setParameters(tradesCountStatement, 0, filterParameters.toArray());
						try (var resultSet = tradesCountStatement.executeQuery()) {
							if (resultSet.next()) {
								totalTradesCount = resultSet.getInt(1);
							}
						}
					}

					if (totalTradesCount == 0) {
						// No trades found:
						return new TradingHistoryResult(trades, totalTradesCount);
					}

					int startIndex = range.getStartIndex(totalTradesCount);
					int endIndex = range.getEndIndex(totalTradesCount);
					int offset = startIndex;
					int limit = (endIndex - startIndex);

					try (var selectTradesStatement = connection.prepareStatement(selectTradesSql)) {
						JdbcUtils.setParameters(selectTradesStatement, 0, filterParameters.toArray());
						JdbcUtils.setParameters(selectTradesStatement, filterParameters.size(), limit, offset);

						try (var resultSet = selectTradesStatement.executeQuery()) {
							while (resultSet.next()) {
								trades.add(this.readTradeRecord(resultSet));
							}
						}
					}

					return new TradingHistoryResult(trades, totalTradesCount);
				});
			} catch (Exception e) {
				throw new RuntimeException("Failed to fetch trading history: " + request.toString(), e);
			}
		}, ((SKShopkeepersPlugin) plugin).getAsyncExecutor());
	}

	private TradeRecord readTradeRecord(ResultSet resultSet) throws SQLException {
		assert resultSet != null;
		Instant timestamp = Instant.parse(Validate.notNull(resultSet.getString(COLUMN_TIMESTAMP)));

		UUID playerUniqueId = UUID.fromString(Validate.notNull(resultSet.getString(COLUMN_PLAYER_UUID)));
		String playerName = Validate.notNull(resultSet.getString(COLUMN_PLAYER_NAME));
		PlayerRecord player = PlayerRecord.of(playerUniqueId, playerName);

		UUID shopUniqueId = UUID.fromString(Validate.notNull(resultSet.getString(COLUMN_SHOP_UUID)));
		String shopType = Validate.notNull(resultSet.getString(COLUMN_SHOP_TYPE));
		@Nullable PlayerRecord owner = null;
		String ownerUniqueIdString = resultSet.getString(COLUMN_SHOP_OWNER_UUID);
		if (ownerUniqueIdString != null) {
			UUID ownerUniqueId = UUID.fromString(ownerUniqueIdString);
			String ownerName = Validate.notNull(resultSet.getString(COLUMN_SHOP_OWNER_NAME));
			owner = PlayerRecord.of(ownerUniqueId, ownerName);
		}
		String shopName = ""; // Not stored
		@Nullable String worldName = resultSet.getString(COLUMN_SHOP_WORLD);
		int shopX = resultSet.getInt(COLUMN_SHOP_X);
		int shopY = resultSet.getInt(COLUMN_SHOP_Y);
		int shopZ = resultSet.getInt(COLUMN_SHOP_Z);
		ShopRecord shop = new ShopRecord(
				shopUniqueId,
				shopType,
				owner,
				shopName,
				worldName,
				shopX,
				shopY,
				shopZ
		);

		String item1Type = resultSet.getString(COLUMN_ITEM_1_TYPE);
		int item1Amount = resultSet.getInt(COLUMN_ITEM_1_AMOUNT);
		@Nullable String item1Metadata = resultSet.getString(COLUMN_ITEM_1_METADATA);
		var item1 = loadItemStack(item1Type, item1Amount, item1Metadata);
		if (item1 == null) {
			throw new RuntimeException("item1 is empty!");
		}

		@Nullable String item2Type = resultSet.getString(COLUMN_ITEM_2_TYPE);
		int item2Amount = resultSet.getInt(COLUMN_ITEM_2_AMOUNT);
		@Nullable String item2Metadata = resultSet.getString(COLUMN_ITEM_2_METADATA);
		var item2 = loadItemStack(item2Type, item2Amount, item2Metadata);

		String resultItemType = resultSet.getString(COLUMN_RESULT_ITEM_TYPE);
		int resultItemAmount = resultSet.getInt(COLUMN_RESULT_ITEM_AMOUNT);
		@Nullable String resultItemMetadata = resultSet.getString(COLUMN_RESULT_ITEM_METADATA);
		var resultItem = loadItemStack(resultItemType, resultItemAmount, resultItemMetadata);
		if (resultItem == null) {
			throw new RuntimeException("resultItem is empty!");
		}

		int tradeCount = resultSet.getInt(COLUMN_TRADE_COUNT);
		return new TradeRecord(
				timestamp,
				player,
				shop,
				resultItem,
				item1,
				item2,
				tradeCount
		);
	}

	private static @Nullable UnmodifiableItemStack loadItemStack(
			@Nullable String itemType,
			int amount,
			@Nullable String metadata
	) {
		if (itemType == null || itemType.isEmpty() || amount <= 0) {
			return null;
		}

		var material = ItemUtils.parseMaterial(itemType);
		if (material == null || !material.isItem()) {
			throw new RuntimeException("Invalid item type: " + itemType);
		}

		ItemStack itemStack;
		try {
			itemStack = TradeLogUtils.loadItemStack(material, amount, metadata);
		} catch (Exception e) {
			Log.debug("Failed to load item stack metadata from history!", e);

			// Continue with the item without the item metadata:
			itemStack = new ItemStack(material, amount);
		}

		return UnmodifiableItemStack.ofNonNull(itemStack);
	}
}
