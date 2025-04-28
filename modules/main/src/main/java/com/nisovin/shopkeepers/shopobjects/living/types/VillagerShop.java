package com.nisovin.shopkeepers.shopobjects.living.types;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.api.ui.DefaultUITypes;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.ShopObjectData;
import com.nisovin.shopkeepers.shopobjects.living.LivingShops;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObjectType;
import com.nisovin.shopkeepers.shopobjects.living.types.villager.VillagerSounds;
import com.nisovin.shopkeepers.ui.UIHandler;
import com.nisovin.shopkeepers.ui.editor.Button;
import com.nisovin.shopkeepers.ui.editor.EditorSession;
import com.nisovin.shopkeepers.ui.editor.ShopkeeperActionButton;
import com.nisovin.shopkeepers.ui.trading.TradingHandler;
import com.nisovin.shopkeepers.util.bukkit.RegistryUtils;
import com.nisovin.shopkeepers.util.data.property.BasicProperty;
import com.nisovin.shopkeepers.util.data.property.Property;
import com.nisovin.shopkeepers.util.data.property.validation.java.IntegerValidators;
import com.nisovin.shopkeepers.util.data.property.value.PropertyValue;
import com.nisovin.shopkeepers.util.data.serialization.InvalidDataException;
import com.nisovin.shopkeepers.util.data.serialization.bukkit.KeyedSerializers;
import com.nisovin.shopkeepers.util.data.serialization.java.NumberSerializers;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;
import com.nisovin.shopkeepers.util.java.MathUtils;

public class VillagerShop extends BabyableShop<Villager> {

	public static final Property<Profession> PROFESSION = new BasicProperty<Profession>()
			.dataKeyAccessor("profession", KeyedSerializers.forRegistry(Profession.class, Registry.VILLAGER_PROFESSION))
			.defaultValue(Profession.NONE)
			.build();

	public static final Property<Villager.Type> VILLAGER_TYPE = new BasicProperty<Villager.Type>()
			.dataKeyAccessor("villagerType", KeyedSerializers.forRegistry(Villager.Type.class, Registry.VILLAGER_TYPE))
			.defaultValue(Villager.Type.PLAINS)
			.build();

	public static final int MIN_VILLAGER_LEVEL = 1;
	public static final int MAX_VILLAGER_LEVEL = 5;
	public static final Property<Integer> VILLAGER_LEVEL = new BasicProperty<Integer>()
			.dataKeyAccessor("villagerLevel", NumberSerializers.INTEGER)
			.validator(IntegerValidators.bounded(MIN_VILLAGER_LEVEL, MAX_VILLAGER_LEVEL))
			.defaultValue(1)
			.build();

	private final PropertyValue<Profession> professionProperty = new PropertyValue<>(PROFESSION)
			.onValueChanged(Unsafe.initialized(this)::applyProfession)
			.build(properties);
	private final PropertyValue<Villager.Type> villagerTypeProperty = new PropertyValue<>(VILLAGER_TYPE)
			.onValueChanged(Unsafe.initialized(this)::applyVillagerType)
			.build(properties);
	private final PropertyValue<Integer> villagerLevelProperty = new PropertyValue<>(VILLAGER_LEVEL)
			.onValueChanged(Unsafe.initialized(this)::applyVillagerLevel)
			.build(properties);

	private final VillagerSounds villagerSounds;

	public VillagerShop(
			LivingShops livingShops,
			SKLivingShopObjectType<VillagerShop> livingObjectType,
			AbstractShopkeeper shopkeeper,
			@Nullable ShopCreationData creationData
	) {
		super(livingShops, livingObjectType, shopkeeper, creationData);
		villagerSounds = new VillagerSounds(Unsafe.initialized(this));
	}

	@Override
	public void load(ShopObjectData shopObjectData) throws InvalidDataException {
		super.load(shopObjectData);
		professionProperty.load(shopObjectData);
		villagerTypeProperty.load(shopObjectData);
		villagerLevelProperty.load(shopObjectData);
	}

	@Override
	public void save(ShopObjectData shopObjectData, boolean saveAll) {
		super.save(shopObjectData, saveAll);
		professionProperty.save(shopObjectData);
		villagerTypeProperty.save(shopObjectData);
		villagerLevelProperty.save(shopObjectData);
	}

	@Override
	public void setup() {
		super.setup();

		if (Settings.simulateVillagerTradingSounds) {
			UIHandler tradingUIHandler = shopkeeper.getUIHandler(DefaultUITypes.TRADING());
			if (tradingUIHandler instanceof TradingHandler) {
				TradingHandler tradingHandler = (TradingHandler) tradingUIHandler;
				tradingHandler.addListener(villagerSounds);
			}
		}
	}

	@Override
	protected void onSpawn() {
		super.onSpawn();
		// TODO I wasn't able to reproduce this myself yet, but according to some reports villager
		// shopkeepers would sometimes lose their profession. Setting their experience to something
		// above 0 is an attempt to resolve this.
		// Related (but shouldn't apply here since we use NoAI mobs):
		// https://hub.spigotmc.org/jira/browse/SPIGOT-4776
		Villager entity = Unsafe.assertNonNull(this.getEntity());
		entity.setVillagerExperience(1);

		// Disable the vanilla ambient sounds if we simulate the ambient and/or trading sounds
		// ourselves:
		if (Settings.simulateVillagerTradingSounds || Settings.simulateVillagerAmbientSounds) {
			entity.setSilent(true);
		}

		this.applyProfession();
		this.applyVillagerType();
		this.applyVillagerLevel();
	}

	@Override
	public void onTick() {
		super.onTick();

		// Ambient sounds:
		if (Settings.simulateVillagerAmbientSounds) {
			villagerSounds.tick();
		}
	}

	@Override
	public List<Button> createEditorButtons() {
		List<Button> editorButtons = super.createEditorButtons();
		editorButtons.add(this.getProfessionEditorButton());
		editorButtons.add(this.getVillagerTypeEditorButton());
		editorButtons.add(this.getVillagerLevelEditorButton());
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
		Villager entity = this.getEntity();
		if (entity == null) return; // Not spawned

		entity.setProfession(this.getProfession());
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
        ItemUtils.setDisplayNameAndLore(
				iconItem,
				Messages.buttonVillagerProfession,
				Messages.buttonVillagerProfessionLore
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

	// VILLAGER TYPE

	public Villager.Type getVillagerType() {
		return villagerTypeProperty.getValue();
	}

	public void setVillagerType(Villager.Type villagerType) {
		villagerTypeProperty.setValue(villagerType);
	}

	public void cycleVillagerType(boolean backwards) {
		this.setVillagerType(RegistryUtils.cycleKeyed(
				Registry.VILLAGER_TYPE,
				this.getVillagerType(),
				backwards
		));
	}

	private void applyVillagerType() {
		Villager entity = this.getEntity();
		if (entity == null) return; // Not spawned

		entity.setVillagerType(this.getVillagerType());
	}

	private ItemStack getVillagerTypeEditorItem() {
		ItemStack iconItem = new ItemStack(Material.LEATHER_CHESTPLATE);
        Villager.Type villagerType = this.getVillagerType();
        if (villagerType == Villager.Type.DESERT) {
            ItemUtils.setLeatherColor(iconItem, Color.ORANGE);
        } else if (villagerType == Villager.Type.JUNGLE) {
            ItemUtils.setLeatherColor(iconItem, Color.YELLOW.mixColors(Color.ORANGE));
        } else if (villagerType == Villager.Type.SAVANNA) {
            ItemUtils.setLeatherColor(iconItem, Color.RED);
        } else if (villagerType == Villager.Type.SNOW) {
            ItemUtils.setLeatherColor(iconItem, DyeColor.CYAN.getColor());
        } else if (villagerType == Villager.Type.SWAMP) {
            ItemUtils.setLeatherColor(iconItem, DyeColor.PURPLE.getColor());
        } else if (villagerType == Villager.Type.TAIGA) {
            ItemUtils.setLeatherColor(iconItem, Color.WHITE.mixDyes(DyeColor.BROWN));
        }
		ItemUtils.setDisplayNameAndLore(
				iconItem,
				Messages.buttonVillagerVariant,
				Messages.buttonVillagerVariantLore
		);
		return iconItem;
	}

	private Button getVillagerTypeEditorButton() {
		return new ShopkeeperActionButton() {
			@Override
			public @Nullable ItemStack getIcon(EditorSession editorSession) {
				return getVillagerTypeEditorItem();
			}

			@Override
			protected boolean runAction(
					EditorSession editorSession,
					InventoryClickEvent clickEvent
			) {
				boolean backwards = clickEvent.isRightClick();
				cycleVillagerType(backwards);
				return true;
			}
		};
	}

	// VILLAGER LEVEL

	public int getVillagerLevel() {
		return villagerLevelProperty.getValue();
	}

	public void setVillagerLevel(int villagerLevel) {
		villagerLevelProperty.setValue(villagerLevel);
	}

	public void cycleVillagerLevel(boolean backwards) {
		int villagerLevel = this.getVillagerLevel();
		int nextLevel;
		if (backwards) {
			nextLevel = villagerLevel - 1;
		} else {
			nextLevel = villagerLevel + 1;
		}
		nextLevel = MathUtils.rangeModulo(nextLevel, MIN_VILLAGER_LEVEL, MAX_VILLAGER_LEVEL);
		this.setVillagerLevel(nextLevel);
	}

	private void applyVillagerLevel() {
		Villager entity = this.getEntity();
		if (entity == null) return; // Not spawned

		entity.setVillagerLevel(this.getVillagerLevel());
	}

	private ItemStack getVillagerLevelEditorItem() {
		ItemStack iconItem;
		switch (this.getVillagerLevel()) {
		default:
		case 1:
			iconItem = new ItemStack(Material.COBBLESTONE);
			break;
		case 2:
			iconItem = new ItemStack(Material.IRON_INGOT);
			break;
		case 3:
			iconItem = new ItemStack(Material.GOLD_INGOT);
			break;
		case 4:
			iconItem = new ItemStack(Material.EMERALD);
			break;
		case 5:
			iconItem = new ItemStack(Material.DIAMOND);
			break;
		}
		assert iconItem != null;
		ItemUtils.setDisplayNameAndLore(
				iconItem,
				Messages.buttonVillagerLevel,
				Messages.buttonVillagerLevelLore
		);
		return iconItem;
	}

	private Button getVillagerLevelEditorButton() {
		return new ShopkeeperActionButton() {
			@Override
			public @Nullable ItemStack getIcon(EditorSession editorSession) {
				return getVillagerLevelEditorItem();
			}

			@Override
			protected boolean runAction(
					EditorSession editorSession,
					InventoryClickEvent clickEvent
			) {
				boolean backwards = clickEvent.isRightClick();
				cycleVillagerLevel(backwards);
				return true;
			}
		};
	}
}
