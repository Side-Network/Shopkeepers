package com.nisovin.shopkeepers.shopobjects.living.types;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.ShopObjectData;
import com.nisovin.shopkeepers.shopobjects.living.LivingShops;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObjectType;
import com.nisovin.shopkeepers.ui.editor.Button;
import com.nisovin.shopkeepers.ui.editor.EditorSession;
import com.nisovin.shopkeepers.ui.editor.ShopkeeperActionButton;
import com.nisovin.shopkeepers.util.bukkit.RegistryUtils;
import com.nisovin.shopkeepers.util.data.property.BasicProperty;
import com.nisovin.shopkeepers.util.data.property.Property;
import com.nisovin.shopkeepers.util.data.property.value.PropertyValue;
import com.nisovin.shopkeepers.util.data.serialization.InvalidDataException;
import com.nisovin.shopkeepers.util.data.serialization.bukkit.KeyedSerializers;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

public class ZombieVillagerShop extends ZombieShop<ZombieVillager> {

	public static final Property<Profession> PROFESSION = new BasicProperty<Profession>()
			.dataKeyAccessor("profession", KeyedSerializers.forRegistry(Profession.class, Registry.VILLAGER_PROFESSION))
			.defaultValue(Profession.NONE)
			.build();

	private final PropertyValue<Profession> professionProperty = new PropertyValue<>(PROFESSION)
			.onValueChanged(Unsafe.initialized(this)::applyProfession)
			.build(properties);

	public ZombieVillagerShop(
			LivingShops livingShops,
			SKLivingShopObjectType<ZombieVillagerShop> livingObjectType,
			AbstractShopkeeper shopkeeper,
			@Nullable ShopCreationData creationData
	) {
		super(livingShops, livingObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ShopObjectData shopObjectData) throws InvalidDataException {
		super.load(shopObjectData);
		professionProperty.load(shopObjectData);
	}

	@Override
	public void save(ShopObjectData shopObjectData, boolean saveAll) {
		super.save(shopObjectData, saveAll);
		professionProperty.save(shopObjectData);
	}

	@Override
	protected void onSpawn() {
		super.onSpawn();
		this.applyProfession();
	}

	@Override
	public List<Button> createEditorButtons() {
		List<Button> editorButtons = super.createEditorButtons();
		editorButtons.add(this.getProfessionEditorButton());
		return editorButtons;
	}

	// PROFESSION

	public Profession getProfession() {
		return professionProperty.getValue();
	}

	public void setProfession(Profession profession) {
		professionProperty.setValue(profession);
	}

	public void cycleProfession(boolean backwards) {
		this.setProfession(RegistryUtils.cycleKeyed(
				Registry.VILLAGER_PROFESSION,
				this.getProfession(),
				backwards
		));
	}

	private void applyProfession() {
		ZombieVillager entity = this.getEntity();
		if (entity == null) return; // Not spawned

		entity.setVillagerProfession(this.getProfession());
	}

	private ItemStack getProfessionEditorItem() {
		ItemStack iconItem;
        Profession profession = this.getProfession();
        if (profession == Profession.ARMORER) {
            iconItem = new ItemStack(Material.BLAST_FURNACE);
        } else if (profession == Profession.BUTCHER) {
            iconItem = new ItemStack(Material.SMOKER);
        } else if (profession == Profession.CARTOGRAPHER) {
            iconItem = new ItemStack(Material.CARTOGRAPHY_TABLE);
        } else if (profession == Profession.CLERIC) {
            iconItem = new ItemStack(Material.BREWING_STAND);
        } else if (profession == Profession.FARMER) {
            iconItem = new ItemStack(Material.WHEAT); // Instead of COMPOSTER
        } else if (profession == Profession.FISHERMAN) {
            iconItem = new ItemStack(Material.FISHING_ROD); // Instead of BARREL
        } else if (profession == Profession.FLETCHER) {
            iconItem = new ItemStack(Material.FLETCHING_TABLE);
        } else if (profession == Profession.LEATHERWORKER) {
            iconItem = new ItemStack(Material.LEATHER); // Instead of CAULDRON
        } else if (profession == Profession.LIBRARIAN) {
            iconItem = new ItemStack(Material.LECTERN);
        } else if (profession == Profession.MASON) {
            iconItem = new ItemStack(Material.STONECUTTER);
        } else if (profession == Profession.SHEPHERD) {
            iconItem = new ItemStack(Material.LOOM);
        } else if (profession == Profession.TOOLSMITH) {
            iconItem = new ItemStack(Material.SMITHING_TABLE);
        } else if (profession == Profession.WEAPONSMITH) {
            iconItem = new ItemStack(Material.GRINDSTONE);
        } else if (profession == Profession.NITWIT) {
            iconItem = new ItemStack(Material.LEATHER_CHESTPLATE);
            ItemUtils.setLeatherColor(iconItem, Color.GREEN);
        } else {
            iconItem = new ItemStack(Material.BARRIER);
        }
		ItemUtils.setDisplayNameAndLore(iconItem,
				Messages.buttonZombieVillagerProfession,
				Messages.buttonZombieVillagerProfessionLore
		);
		return iconItem;
	}

	private Button getProfessionEditorButton() {
		return new ShopkeeperActionButton() {
			@Override
			public @Nullable ItemStack getIcon(EditorSession editorSession) {
				return getProfessionEditorItem();
			}

			@Override
			protected boolean runAction(
					EditorSession editorSession,
					InventoryClickEvent clickEvent
			) {
				boolean backwards = clickEvent.isRightClick();
				cycleProfession(backwards);
				return true;
			}
		};
	}
}
