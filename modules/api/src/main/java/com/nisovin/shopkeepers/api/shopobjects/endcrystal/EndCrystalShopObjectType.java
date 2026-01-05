package com.nisovin.shopkeepers.api.shopobjects.endcrystal;

import com.nisovin.shopkeepers.api.shopobjects.ShopObjectType;
import com.nisovin.shopkeepers.api.shopobjects.entity.EntityShopObjectType;

/**
 * A {@link ShopObjectType} of shop objects that end crystal entities to represent the shopkeepers.
 *
 * @param <T>
 *            the type of the shop objects that this represents
 */
public interface EndCrystalShopObjectType<T extends EndCrystalShopObject> extends EntityShopObjectType<T> {
}
