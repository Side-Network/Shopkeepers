package com.nisovin.shopkeepers.shopobjects.living.types.villager;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

/**
 * Villager editor items shared across the villager shopkeeper, zombie villager shopkeeper and
 * normal villager editor.
 */
public class VillagerEditorItems {

	private static final Map<Profession, Supplier<ItemStack>> PROFESSION_EDITOR_ITEMS = Map.ofEntries(
			Map.entry(Profession.ARMORER, () -> new ItemStack(Material.BLAST_FURNACE)),
			Map.entry(Profession.BUTCHER, () -> new ItemStack(Material.SMOKER)),
			Map.entry(Profession.CARTOGRAPHER, () -> new ItemStack(Material.CARTOGRAPHY_TABLE)),
			Map.entry(Profession.CLERIC, () -> new ItemStack(Material.BREWING_STAND)),
			Map.entry(Profession.FARMER, () -> {
				return new ItemStack(Material.WHEAT); // Instead of COMPOSTER
			}),
			Map.entry(Profession.FISHERMAN, () -> {
				return new ItemStack(Material.FISHING_ROD); // Instead of BARREL
			}),
			Map.entry(Profession.FLETCHER, () -> new ItemStack(Material.FLETCHING_TABLE)),
			Map.entry(Profession.LEATHERWORKER, () -> {
				return new ItemStack(Material.LEATHER); // Instead of CAULDRON
			}),
			Map.entry(Profession.LIBRARIAN, () -> new ItemStack(Material.LECTERN)),
			Map.entry(Profession.MASON, () -> new ItemStack(Material.STONECUTTER)),
			Map.entry(Profession.SHEPHERD, () -> new ItemStack(Material.LOOM)),
			Map.entry(Profession.TOOLSMITH, () -> new ItemStack(Material.SMITHING_TABLE)),
			Map.entry(Profession.WEAPONSMITH, () -> new ItemStack(Material.GRINDSTONE)),
			Map.entry(Profession.NITWIT, () -> {
				var item = new ItemStack(Material.LEATHER_CHESTPLATE);
				return ItemUtils.setLeatherColor(item, Color.GREEN);
			}),
			Map.entry(Profession.NONE, () -> new ItemStack(Material.BARRIER))
	);

	public static ItemStack getProfessionEditorItem(Profession profession) {
		var itemSupplier = PROFESSION_EDITOR_ITEMS.getOrDefault(profession, () -> {
			return new ItemStack(Material.BARRIER);
		});
		ItemStack iconItem = itemSupplier.get();
		assert iconItem != null;
		return iconItem;
	}

	private static final Map<Villager.Type, Optional<Color>> VILLAGER_TYPE_EDITOR_ITEM_COLORS = Map.ofEntries(
			Map.entry(Villager.Type.PLAINS, Optional.empty()), // Default brown color
			Map.entry(Villager.Type.DESERT, Unsafe.castNonNull(Optional.of(Color.ORANGE))),
			Map.entry(Villager.Type.JUNGLE, Unsafe.castNonNull(Optional.of(Color.YELLOW.mixColors(Color.ORANGE)))),
			Map.entry(Villager.Type.SAVANNA, Unsafe.castNonNull(Optional.of(Color.RED))),
			Map.entry(Villager.Type.SNOW, Unsafe.castNonNull(Optional.of(DyeColor.CYAN.getColor()))),
			Map.entry(Villager.Type.SWAMP, Unsafe.castNonNull(Optional.of(DyeColor.PURPLE.getColor()))),
			Map.entry(Villager.Type.TAIGA, Unsafe.castNonNull(Optional.of(Color.WHITE.mixDyes(DyeColor.BROWN))))
	);

	public static ItemStack getVillagerTypeEditorItem(Villager.Type villagerType) {
		ItemStack iconItem = new ItemStack(Material.LEATHER_CHESTPLATE);

		var color = VILLAGER_TYPE_EDITOR_ITEM_COLORS.getOrDefault(villagerType, Optional.empty()).orElse(null);
		if (color != null) {
			ItemUtils.setLeatherColor(iconItem, color);
		} // Else: Default brown color.

		return iconItem;
	}

	private VillagerEditorItems() {
	}
}
