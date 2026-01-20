package com.nisovin.shopkeepers.shopkeeper.admin.regular;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.shopkeeper.admin.AbstractAdminShopkeeper.AdminShopTradingViewProvider;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;

public class RegularAdminShopTradingViewProvider extends AdminShopTradingViewProvider {

	protected RegularAdminShopTradingViewProvider(SKRegularAdminShopkeeper shopkeeper) {
		super(shopkeeper);
	}

	@Override
	public SKRegularAdminShopkeeper getShopkeeper() {
		return (SKRegularAdminShopkeeper) super.getShopkeeper();
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new RegularAdminShopTradingView(this, player, uiState);
	}
}
