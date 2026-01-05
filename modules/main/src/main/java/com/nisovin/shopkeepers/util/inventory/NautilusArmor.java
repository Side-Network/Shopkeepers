package com.nisovin.shopkeepers.util.inventory;

import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.compat.MC_1_21_11;

public enum NautilusArmor {

	COPPER(MC_1_21_11.COPPER_NAUTILUS_ARMOR),
	GOLDEN(MC_1_21_11.GOLDEN_NAUTILUS_ARMOR),
	IRON(MC_1_21_11.IRON_NAUTILUS_ARMOR),
	DIAMOND(MC_1_21_11.DIAMOND_NAUTILUS_ARMOR),
	NETHERITE(MC_1_21_11.NETHERITE_NAUTILUS_ARMOR);

	private final @Nullable Material material;

	private NautilusArmor(@Nullable Material material) {
		this.material = material;
	}

	public @Nullable Material getMaterial() {
		return material;
	}

	public boolean isEnabled() {
		return material != null;
	}
}
