package com.nisovin.shopkeepers.shopobjects.living;

import java.util.List;

import org.bukkit.entity.EntityType;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.nisovin.shopkeepers.api.shopobjects.living.LivingShopObjectType;
import com.nisovin.shopkeepers.config.Settings.DerivedSettings;
import com.nisovin.shopkeepers.shopobjects.entity.base.BaseEntityShopObjectType;
import com.nisovin.shopkeepers.util.java.Validate;

public final class SKLivingShopObjectType<T extends SKLivingShopObject<?>>
		extends BaseEntityShopObjectType<T> implements LivingShopObjectType<T> {

	protected SKLivingShopObjectType(
			LivingShopObjectCreationContext shopCreationContext,
			EntityType entityType,
			Class<@NonNull T> shopObjectType,
			ShopObjectConstructor<T> shopObjectConstructor
	) {
		this(
				shopCreationContext,
				entityType,
				getIdentifier(entityType),
				getAliasesFor(entityType),
				getPermission(entityType),
				shopObjectType,
				shopObjectConstructor
		);
	}

	protected SKLivingShopObjectType(
			LivingShopObjectCreationContext shopCreationContext,
			EntityType entityType,
			String identifier,
			List<? extends String> aliases,
			String permission,
			Class<@NonNull T> shopObjectType,
			ShopObjectConstructor<T> shopObjectConstructor
	) {
		super(
				shopCreationContext,
				entityType,
				identifier,
				aliases,
				permission,
				shopObjectType,
				shopObjectConstructor
		);
		Validate.isTrue(entityType.isAlive(), "entityType is not alive");
	}

	@Override
	public boolean isEnabled() {
		return DerivedSettings.enabledLivingShops.contains(entityType);
	}
}
