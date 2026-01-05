package com.nisovin.shopkeepers.shopobjects.living.types;

import java.util.List;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Bat;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.ShopObjectData;
import com.nisovin.shopkeepers.shopobjects.entity.base.BaseEntityShopObjectCreationContext;
import com.nisovin.shopkeepers.shopobjects.entity.base.BaseEntityShopObjectType;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObject;
import com.nisovin.shopkeepers.ui.editor.Button;
import com.nisovin.shopkeepers.util.data.serialization.InvalidDataException;

public class BatShop extends SKLivingShopObject<Bat> {

	public BatShop(
			BaseEntityShopObjectCreationContext context,
			BaseEntityShopObjectType<BatShop> shopObjectType,
			AbstractShopkeeper shopkeeper,
			@Nullable ShopCreationData creationData
	) {
		super(context, shopObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ShopObjectData shopObjectData) throws InvalidDataException {
		super.load(shopObjectData);
	}

	@Override
	public void save(ShopObjectData shopObjectData, boolean saveAll) {
		super.save(shopObjectData, saveAll);
	}

	@Override
	protected void onSpawn() {
		super.onSpawn();
	}

	@Override
	public void tickAI() {
		super.tickAI();

		Bat entity = Unsafe.assertNonNull(this.getEntity());
		var hasSolidBlockAbove = entity.getLocation().getBlock()
				.getRelative(BlockFace.UP)
				.getType().isSolid();
		entity.setAwake(!hasSolidBlockAbove);
	}

	@Override
	public List<Button> createEditorButtons() {
		List<Button> editorButtons = super.createEditorButtons();
		return editorButtons;
	}
}
