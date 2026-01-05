package com.nisovin.shopkeepers.shopcreation;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.ShopType;
import com.nisovin.shopkeepers.api.shopobjects.ShopObjectType;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.config.Settings.DerivedSettings;
import com.nisovin.shopkeepers.util.annotations.ReadOnly;
import com.nisovin.shopkeepers.util.annotations.ReadWrite;
import com.nisovin.shopkeepers.util.bukkit.NamespacedKeyUtils;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

/**
 * Helpers related to the shop creation item.
 */
public final class ShopCreationItem {

	/**
	 * The item persistent data {@link NamespacedKey} by which we identify the shop creation item.
	 */
	private static final NamespacedKey KEY_SHOP_CREATION_ITEM
			= NamespacedKeyUtils.create("shopkeepers", "shop_creation_item");

	/**
	 * The item persistent data {@link NamespacedKey} that determines the fixed shop type, if
	 * present.
	 */
	private static final NamespacedKey KEY_SHOP_TYPE
			= NamespacedKeyUtils.create("shopkeepers", "shop_type");

	/**
	 * The item persistent data {@link NamespacedKey} that determines the fixed shop object type, if
	 * present.
	 */
	private static final NamespacedKey KEY_OBJECT_TYPE
			= NamespacedKeyUtils.create("shopkeepers", "object_type");

	public static ItemStack create() {
		return create(1);
	}

	public static ItemStack create(int amount) {
		return DerivedSettings.shopCreationItemData.createItemStack(amount);
	}

	public static boolean isShopCreationItem(@Nullable UnmodifiableItemStack itemStack) {
		return isShopCreationItem(ItemUtils.asItemStackOrNull(itemStack));
	}

	public static boolean isShopCreationItem(@ReadOnly @Nullable ItemStack itemStack) {
		return new ShopCreationItem(itemStack).isShopCreationItem();
	}

	// ----

	private final @Nullable ItemStack itemStack;
	private @Nullable ItemMeta itemMeta;

	public ShopCreationItem(@ReadWrite @Nullable ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	private @Nullable ItemMeta getItemMeta() {
		if (itemMeta != null) {
			return itemMeta;
		}

		if (ItemUtils.isEmpty(itemStack)) {
			return null;
		}
		assert itemStack != null;

		itemMeta = Unsafe.assertNonNull(itemStack.getItemMeta());
		return itemMeta;
	}

	/**
	 * Applies any item meta changes done via this wrapper class back to the item stack.
	 */
	public void applyItemMeta() {
		if (itemMeta == null) {
			return; // No changes to apply
		}

		if (itemStack == null) {
			return; // Nothing to apply
		}

		assert itemStack != null;
		assert itemMeta != null;
		itemStack.setItemMeta(itemMeta);
	}

	public boolean isShopCreationItem() {
		if (Settings.identifyShopCreationItemByTag) {
			return this.hasTag();
		} else {
			return Settings.shopCreationItem.matches(itemStack);
		}
	}

	/**
	 * Checks if the item stack has a shop creation item tag set.
	 * 
	 * @return <code>true</code> if the item stack has the shop creation item tag set
	 */
	public boolean hasTag() {
		ItemMeta meta = this.getItemMeta();
		if (meta == null) return false;

		PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
		return dataContainer.has(KEY_SHOP_CREATION_ITEM);
	}

	/**
	 * Applies the shop creation item tag to the item stack's persistent data.
	 * <p>
	 * Use {@link #applyItemMeta()} to apply the changes to the underlying item stack.
	 * 
	 * @return <code>true</code> if the tag was freshly added
	 */
	public boolean addTag() {
		ItemMeta meta = this.getItemMeta();
		if (meta == null) return false;

		PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
		if (dataContainer.has(KEY_SHOP_CREATION_ITEM)) {
			return false;
		}

		dataContainer.set(KEY_SHOP_CREATION_ITEM, PersistentDataType.BOOLEAN, true);
		return true;
	}

	/**
	 * Checks if the item stack has a shop type set.
	 * <p>
	 * This does not check if the set shop type id is valid.
	 * 
	 * @return <code>true</code> if a shop type is set
	 */
	public boolean hasShopType() {
		ItemMeta meta = this.getItemMeta();
		if (meta == null) return false;

		PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
		return dataContainer.has(KEY_SHOP_TYPE);
	}

	/**
	 * Gets the shop type id from the item stack's persistent data.
	 * 
	 * @return the shop type id, <code>null</code> if not present, an empty string if the key is
	 *         present but the value cannot be read as string
	 */
	public @Nullable String getShopTypeId() {
		ItemMeta meta = this.getItemMeta();
		if (meta == null) return null;

		PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

		try {
			return dataContainer.get(KEY_SHOP_TYPE, PersistentDataType.STRING);
		} catch (IllegalArgumentException e) {
			// Return an empty string if the key is present, but we are not able to read the value:
			return dataContainer.has(KEY_SHOP_TYPE) ? "" : null;
		}
	}

	/**
	 * Applies the shop type to the item stack's persistent data.
	 * <p>
	 * This replaces any previously set shop type on the item stack.
	 * <p>
	 * Use {@link #applyItemMeta()} to apply the changes to the underlying item stack.
	 * 
	 * @param shopType
	 *            the shop type
	 * @return <code>true</code> if the shop type was successfully applied
	 */
	public boolean setShopType(ShopType<?> shopType) {
		ItemMeta meta = this.getItemMeta();
		if (meta == null) return false;

		PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
		dataContainer.set(KEY_SHOP_TYPE, PersistentDataType.STRING, shopType.getIdentifier());
		return true;
	}

	/**
	 * Checks if the item stack has a shop object type set.
	 * <p>
	 * This does not check if the set object type id is valid.
	 * 
	 * @return <code>true</code> if a shop object type is set
	 */
	public boolean hasObjectType() {
		ItemMeta meta = this.getItemMeta();
		if (meta == null) return false;

		PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
		return dataContainer.has(KEY_OBJECT_TYPE);
	}

	/**
	 * Gets the object type id from the item stack's persistent data.
	 * 
	 * @return the shop object type id, <code>null</code> if not present, an empty string if the key
	 *         is present but the value cannot be read as string
	 */
	public @Nullable String getObjectTypeId() {
		ItemMeta meta = this.getItemMeta();
		if (meta == null) return null;

		PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

		try {
			return dataContainer.get(KEY_OBJECT_TYPE, PersistentDataType.STRING);
		} catch (IllegalArgumentException e) {
			// Return an empty string if the key is present, but we are not able to read the value:
			return dataContainer.has(KEY_OBJECT_TYPE) ? "" : null;
		}
	}

	/**
	 * Applies the object type to the item stack's persistent data.
	 * <p>
	 * This replaces any previously set object type on the item stack.
	 * <p>
	 * Use {@link #applyItemMeta()} to apply the changes to the underlying item stack.
	 * 
	 * @param objectType
	 *            the shop object type
	 * @return <code>true</code> if the object type was successfully applied
	 */
	public boolean setObjectType(ShopObjectType<?> objectType) {
		ItemMeta meta = this.getItemMeta();
		if (meta == null) return false;

		PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
		dataContainer.set(KEY_OBJECT_TYPE, PersistentDataType.STRING, objectType.getIdentifier());
		return true;
	}
}
