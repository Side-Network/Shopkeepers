package com.nisovin.shopkeepers.shopkeeper.admin;

import com.nisovin.shopkeepers.shopkeeper.player.AbstractPlayerShopkeeper;
import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.ui.trading.Trade;
import com.nisovin.shopkeepers.ui.trading.TradingContext;
import com.nisovin.shopkeepers.ui.trading.TradingHandler;

public abstract class AdminShopTradingHandler extends TradingHandler {

    protected AdminShopTradingHandler(AbstractAdminShopkeeper shopkeeper) {
        super(shopkeeper);
    }

    @Override
    public AbstractAdminShopkeeper getShopkeeper() {
        return (AbstractAdminShopkeeper) super.getShopkeeper();
    }

    @Override
    public boolean canOpen(Player player, boolean silent) {
        if (!super.canOpen(player, silent)) return false;
        return true;
    }

    @Override
    protected boolean prepareTrade(Trade trade) {
        if (!super.prepareTrade(trade)) return false;

        return true;
    }

    @Override
    protected void onTradeApplied(Trade trade) {
        super.onTradeApplied(trade);


    }

    @Override
    protected void onTradeOver(TradingContext tradingContext) {
        super.onTradeOver(tradingContext);


    }
}
