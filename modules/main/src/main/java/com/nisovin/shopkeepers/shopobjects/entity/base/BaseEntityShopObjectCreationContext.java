package com.nisovin.shopkeepers.shopobjects.entity.base;

/**
 * Constant context passed by the shop object type to the shop object during construction.
 * <p>
 * Can be extended by sub-types.
 */
public class BaseEntityShopObjectCreationContext {

	public final BaseEntityShops baseEntityShops;

	public BaseEntityShopObjectCreationContext(BaseEntityShops baseEntityShops) {
		this.baseEntityShops = baseEntityShops;
	}
}
