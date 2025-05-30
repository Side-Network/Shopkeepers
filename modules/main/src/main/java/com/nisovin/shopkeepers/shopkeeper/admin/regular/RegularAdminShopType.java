package com.nisovin.shopkeepers.shopkeeper.admin.regular;

import java.util.Collections;
import java.util.List;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.admin.AbstractAdminShopType;
import org.bukkit.Bukkit;

import static com.nisovin.shopkeepers.lang.Messages.shopSetupDescAdminRegular;

public final class RegularAdminShopType
		extends AbstractAdminShopType<SKRegularAdminShopkeeper> {

	public RegularAdminShopType() {
		super(
				"admin",
				Collections.emptyList(),
				ShopkeepersPlugin.ADMIN_PERMISSION,
				SKRegularAdminShopkeeper.class
		);
	}

	@Override
	public String getDisplayName() {
		return Messages.shopTypeAdminRegular;
	}

	@Override
	public String getDescription() {
		return Messages.shopTypeDescAdminRegular;
	}

	@Override
	public String getSetupDescription() {
		Bukkit.broadcastMessage("getSetupDescription");
		return shopSetupDescAdminRegular;
	}

	@Override
	public List<? extends String> getTradeSetupDescription() {
		return Messages.tradeSetupDescAdminRegular;
	}

	@Override
	protected SKRegularAdminShopkeeper createNewShopkeeper() {
		return new SKRegularAdminShopkeeper();
	}
}
