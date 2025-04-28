package com.nisovin.shopkeepers.shopkeeper.admin.regular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nisovin.shopkeepers.api.shopkeeper.offers.PriceOffer;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.currency.Currencies;
import com.nisovin.shopkeepers.currency.Currency;
import com.nisovin.shopkeepers.shopkeeper.SKTradingRecipe;
import com.nisovin.shopkeepers.ui.trading.Trade;
import com.nisovin.shopkeepers.util.inventory.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import com.nisovin.shopkeepers.api.ui.DefaultUITypes;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopkeeper.SKDefaultShopTypes;
import com.nisovin.shopkeepers.shopkeeper.ShopkeeperData;
import com.nisovin.shopkeepers.shopkeeper.admin.AbstractAdminShopkeeper;
import com.nisovin.shopkeepers.shopkeeper.migration.Migration;
import com.nisovin.shopkeepers.shopkeeper.migration.MigrationPhase;
import com.nisovin.shopkeepers.shopkeeper.migration.ShopkeeperDataMigrator;
import com.nisovin.shopkeepers.shopkeeper.offers.SKTradeOffer;
import com.nisovin.shopkeepers.util.annotations.ReadWrite;
import com.nisovin.shopkeepers.util.data.property.BasicProperty;
import com.nisovin.shopkeepers.util.data.property.Property;
import com.nisovin.shopkeepers.util.data.serialization.InvalidDataException;
import com.nisovin.shopkeepers.util.java.CollectionUtils;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

public class SKRegularAdminShopkeeper
		extends AbstractAdminShopkeeper implements RegularAdminShopkeeper {

	// There can be multiple different offers for the same kind of item:
	private final List<SKTradeOffer> offers = new ArrayList<>();
	private final List<? extends SKTradeOffer> offersView = Collections.unmodifiableList(offers);

	/**
	 * Creates a not yet initialized {@link SKRegularAdminShopkeeper}.
	 * <p>
	 * See {@link AbstractShopkeeper} for details on initialization.
	 */
	protected SKRegularAdminShopkeeper() {
	}

	@Override
	protected void setup() {
		this.registerUIHandlerIfMissing(DefaultUITypes.EDITOR(), () -> {
			return new RegularAdminShopEditorHandler(this);
		});
		super.setup();
	}

	@Override
	public void loadDynamicState(ShopkeeperData shopkeeperData) throws InvalidDataException {
		super.loadDynamicState(shopkeeperData);
		this.loadOffers(shopkeeperData);
	}

	@Override
	public void saveDynamicState(ShopkeeperData shopkeeperData, boolean saveAll) {
		super.saveDynamicState(shopkeeperData, saveAll);
		this.saveOffers(shopkeeperData);
	}

	// ITEM UPDATES

	@Override
	protected int updateItems(String logPrefix, @ReadWrite ShopkeeperData shopkeeperData) {
		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.updateItems");
		int updatedItems = super.updateItems(logPrefix, shopkeeperData);
		updatedItems += updateOfferItems(logPrefix, shopkeeperData);
		return updatedItems;
	}

	private static int updateOfferItems(String logPrefix, @ReadWrite ShopkeeperData shopkeeperData) {
		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.updateOfferItems");
		try {
			var updatedOffers = new ArrayList<TradeOffer>(shopkeeperData.get(OFFERS));
			var updatedItems = SKTradeOffer.updateItems(updatedOffers, logPrefix);
			if (updatedItems > 0) {
				shopkeeperData.set(OFFERS, updatedOffers);
				return updatedItems;
			}
		} catch (InvalidDataException e) {
			Log.warning(logPrefix + "Failed to load '" + OFFERS.getName() + "'!", e);
		}
		return 0;
	}

	//

	@Override
	public RegularAdminShopType getType() {
		return SKDefaultShopTypes.ADMIN_REGULAR();
	}

	@Override
	public boolean hasTradingRecipes(@Nullable Player player) {
		return !this.getOffers().isEmpty();
	}

	private static int getMaximumSellingPrice() {
		// Combined value of two stacks of the two highest valued currencies:
		// TODO In the future: Two stacks of the single highest valued currency.
		int maxPrice = 0;
		int currenciesCount = Currencies.getAll().size();
		Currency currency1 = Currencies.getAll().get(currenciesCount - 1);
		maxPrice += currency1.getStackValue();

		if (currenciesCount > 1) {
			Currency currency2 = Currencies.getAll().get(currenciesCount - 2);
			maxPrice += currency2.getStackValue();
		}
		return maxPrice;
	}

	protected final @Nullable TradingRecipe createSellingRecipe(
			UnmodifiableItemStack itemBeingSold,
			int price,
			boolean outOfStock
	) {
		Validate.notNull(itemBeingSold, "itemBeingSold is null");
		Validate.isTrue(price > 0, "price has to be positive");

		UnmodifiableItemStack item1 = null;
		UnmodifiableItemStack item2 = null;

		int remainingPrice = price;
		if (Currencies.isHighCurrencyEnabled() && price > Settings.highCurrencyMinCost) {
			Currency highCurrency = Currencies.getHigh();
			int highCurrencyAmount = Math.min(
					price / highCurrency.getValue(),
					highCurrency.getMaxStackSize()
			);
			if (highCurrencyAmount > 0) {
				remainingPrice -= (highCurrencyAmount * highCurrency.getValue());
				UnmodifiableItemStack highCurrencyItem = highCurrency.getItemData().createUnmodifiableItemStack(highCurrencyAmount);
				item1 = highCurrencyItem; // Using the first slot
			}
		}

		if (remainingPrice > 0) {
			Currency baseCurrency = Currencies.getBase();
			int maxStackSize = baseCurrency.getMaxStackSize();
			if (remainingPrice > maxStackSize) {
				// Cannot represent this price with the used currency items:
				// TODO Move this warning into the loading phase.
				int maxPrice = getMaximumSellingPrice();
				Log.warning(this.getLogPrefix() + "Skipping offer with invalid price (" + price
						+ "). Maximum price is " + maxPrice + ".");
				return null;
			}

			UnmodifiableItemStack currencyItem = baseCurrency.getItemData().createUnmodifiableItemStack(remainingPrice);
			if (item1 == null) {
				item1 = currencyItem;
			} else {
				// The first item of the trading recipe is already used by the high currency item:
				item2 = currencyItem;
			}
		}
		assert item1 != null;
		return new SKTradingRecipe(itemBeingSold, item1, item2, outOfStock);
	}

	@Override
	public List<? extends TradingRecipe> getTradingRecipes(@Nullable Player player) {
		// Empty if the container is not found:
		List<? extends TradeOffer> offers = this.getOffers();
		List<TradingRecipe> recipes = new ArrayList<>(offers.size());
		offers.forEach(offer -> {
			// Both the offer's and the trading recipe's items are immutable. So there is no need to
			// copy the item.
			UnmodifiableItemStack tradedItem = offer.getItem1();
			Bukkit.broadcastMessage(offer.getStock() + "");
			boolean outOfStock = true;
			TradingRecipe recipe = this.createSellingRecipe(
					tradedItem,
					1,
					outOfStock
			);
			if (recipe != null) {
				recipes.add(recipe);
			} // Else: Price is invalid (cannot be represented by currency items).
		});
		return Collections.unmodifiableList(recipes);
	}

//	@Override
//	public List<? extends TradingRecipe> getTradingRecipes(@Nullable Player player) {
//		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.getTradingRecipes");
//		// SKTradeOffer extends SKTradingRecipe and reports to not be out-of-stock. Both
//		// SKTradeOffer and TradingRecipe are immutable. We can therefore reuse the offers as
//		// trading recipes, and don't have to create new trading recipes for them.
//		return offersView;
//	}

	// OFFERS

	private static final String DATA_KEY_OFFERS = "recipes";
	public static final Property<List<? extends TradeOffer>> OFFERS = new BasicProperty<List<? extends TradeOffer>>()
			.dataKeyAccessor(DATA_KEY_OFFERS, SKTradeOffer.LIST_SERIALIZER)
			.useDefaultIfMissing()
			.defaultValue(Collections.emptyList())
			.build();

	static {
		// Register shopkeeper data migrations:
		ShopkeeperDataMigrator.registerMigration(new Migration(
				"admin-offers",
				MigrationPhase.ofShopkeeperClass(SKRegularAdminShopkeeper.class)
		) {
			@Override
			public boolean migrate(
					ShopkeeperData shopkeeperData,
					String logPrefix
			) throws InvalidDataException {
				return SKTradeOffer.migrateOffers(
						shopkeeperData.getDataValue(DATA_KEY_OFFERS),
						logPrefix
				);
			}
		});
	}

	private void loadOffers(ShopkeeperData shopkeeperData) throws InvalidDataException {
		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.loadOffers");
		assert shopkeeperData != null;
		this._setOffers(shopkeeperData.get(OFFERS));
	}

	private void saveOffers(ShopkeeperData shopkeeperData) {
		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.saveOffers");
		this.getOffers().forEach(offer -> {
			offer.setStock(10);
		});
		assert shopkeeperData != null;
		shopkeeperData.set(OFFERS, this.getOffers());
	}

	@Override
	public List<? extends TradeOffer> getOffers() {
		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.getOffers");
		return offersView;
	}

	@Override
	public void clearOffers() {
		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.clearOffers");
		this._clearOffers();
		this.markDirty();
	}

	private void _clearOffers() {
		offers.clear();
	}

	@Override
	public void setOffers(List<? extends TradeOffer> offers) {
		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.setOffers");
		Validate.notNull(offers, "offers is null");
		Validate.noNullElements(offers, "offers contains null");
		this._setOffers(offers);
		this.markDirty();
	}

	private void _setOffers(List<? extends TradeOffer> offers) {
		assert offers != null && !CollectionUtils.containsNull(offers);
		this._clearOffers();
		this._addOffers(offers);
	}

	@Override
	public void addOffer(TradeOffer offer) {
		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.addOffer");
		Validate.notNull(offer, "offer is null");
		this._addOffer(offer);
		this.markDirty();
	}

	private void _addOffer(TradeOffer offer) {
		assert offer != null;
		Validate.isTrue(offer instanceof SKTradeOffer, "offer is not of type SKTradeOffer");
		SKTradeOffer skOffer = (SKTradeOffer) offer;

		// Add the new offer:
		offers.add(skOffer);
	}

	@Override
	public void addOffers(List<? extends TradeOffer> offers) {
		Bukkit.broadcastMessage("SKRegularAdminShopkeeper.addOffers");
		Validate.notNull(offers, "offers is null");
		Validate.noNullElements(offers, "offers contains null");
		this._addOffers(offers);
		this.markDirty();
	}

	private void _addOffers(List<? extends TradeOffer> offers) {
		assert offers != null && !CollectionUtils.containsNull(offers);
		offers.forEach(this::_addOffer);
	}
}
