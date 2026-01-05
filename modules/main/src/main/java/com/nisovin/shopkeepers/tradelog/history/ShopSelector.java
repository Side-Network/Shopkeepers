package com.nisovin.shopkeepers.tradelog.history;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

public interface ShopSelector {

	public static final ShopSelector ALL = new ShopSelector() {
		@Override
		public String toString() {
			return "ShopSelector.ALL";
		}
	};

	public static final ShopSelector ADMIN_SHOPS = new ShopSelector() {
		@Override
		public String toString() {
			return "ShopSelector.ADMIN_SHOPS";
		}
	};

	public static final ShopSelector PLAYER_SHOPS = new ShopSelector() {
		@Override
		public String toString() {
			return "ShopSelector.PLAYER_SHOPS";
		}
	};

	public static abstract class ByShopIdentifier implements ShopSelector {

		private final @Nullable UUID ownerUUID; // Null to ignore owner
		// The player name, if known:
		private final @Nullable String ownerName;

		public ByShopIdentifier(@Nullable UUID ownerUUID, @Nullable String ownerName) {
			this.ownerUUID = ownerUUID;
			this.ownerName = ownerName;
		}

		/**
		 * The owner uuid, or <code>null</code> if no owner is specified.
		 * 
		 * @return the owner uuid, or <code>null</code> to ignore the owner
		 */
		public @Nullable UUID getOwnerUUID() {
			return ownerUUID;
		}

		/**
		 * The owner name, or <code>null</code> if no owner is specified or the name is not known.
		 * 
		 * @return the owner name, or <code>null</code> if not available
		 */
		public @Nullable String getOwnerName() {
			return ownerName;
		}

		public abstract Text getShopIdentifier();
	}

	public static class ByShopUUID extends ByShopIdentifier {

		private final UUID shopUUID;

		public ByShopUUID(UUID shopUUID) {
			this(shopUUID, null, null);
		}

		public ByShopUUID(UUID shopUUID, @Nullable UUID ownerUUID, @Nullable String ownerName) {
			super(ownerUUID, ownerName);
			Validate.notNull(shopUUID, "Shop uuid is null!");
			this.shopUUID = shopUUID;
		}

		public UUID getShopUUID() {
			return shopUUID;
		}

		@Override
		public Text getShopIdentifier() {
			return Text.of(shopUUID.toString());
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ShopSelector.ByShopUUID [shopUUID=");
			builder.append(shopUUID);
			builder.append(", ownerUUID=");
			builder.append(this.getOwnerUUID());
			builder.append("]");
			return builder.toString();
		}
	}

	public static class ByExistingShop extends ByShopUUID {

		private final Shopkeeper shopkeeper;

		public ByExistingShop(Shopkeeper shopkeeper) {
			this(shopkeeper, null, null);
		}

		public ByExistingShop(
				Shopkeeper shopkeeper,
				@Nullable UUID ownerUUID,
				@Nullable String ownerName
		) {
			super(Validate.notNull(shopkeeper).getUniqueId(), ownerUUID, ownerName);
			this.shopkeeper = shopkeeper;
		}

		public Shopkeeper getShopkeeper() {
			return shopkeeper;
		}

		@Override
		public Text getShopIdentifier() {
			return TextUtils.getShopText(shopkeeper);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ShopSelector.ByExistingShop [shopUUID=");
			builder.append(shopkeeper.getUniqueId());
			builder.append(", ownerUUID=");
			builder.append(this.getOwnerUUID());
			builder.append("]");
			return builder.toString();
		}
	}

	public static class ByOwnerUUID implements ShopSelector {

		private final UUID ownerUUID;
		// The player name, if known:
		private final @Nullable String ownerName;

		public ByOwnerUUID(UUID ownerUUID, @Nullable String ownerName) {
			Validate.notNull(ownerUUID, "Owner uuid is null!");
			this.ownerUUID = ownerUUID;
			this.ownerName = ownerName;
		}

		public UUID getOwnerUUID() {
			return ownerUUID;
		}

		public @Nullable String getOwnerName() {
			return ownerName;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ShopSelector.ByOwnerUUID [ownerUUID=");
			builder.append(ownerUUID);
			builder.append(", ownerName=");
			builder.append(ownerName);
			builder.append("]");
			return builder.toString();
		}
	}
}
