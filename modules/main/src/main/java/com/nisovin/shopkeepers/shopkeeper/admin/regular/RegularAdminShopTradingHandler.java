package com.nisovin.shopkeepers.shopkeeper.admin.regular;

import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.admin.AbstractAdminShopkeeper;
import com.nisovin.shopkeepers.shopkeeper.admin.AdminShopTradingHandler;
import com.nisovin.shopkeepers.shopkeeper.player.trade.SKTradingPlayerShopkeeper;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import org.bukkit.Bukkit;

import com.nisovin.shopkeepers.ui.trading.Trade;
import com.nisovin.shopkeepers.ui.trading.TradingContext;
import org.bukkit.entity.Player;

public class RegularAdminShopTradingHandler extends AdminShopTradingHandler {


    protected RegularAdminShopTradingHandler(SKRegularAdminShopkeeper shopkeeper) {
        super(shopkeeper);
    }

    @Override
    public SKRegularAdminShopkeeper getShopkeeper() {
        return (SKRegularAdminShopkeeper) super.getShopkeeper();
    }

    @Override
    protected boolean prepareTrade(Trade trade) {
        if (!super.prepareTrade(trade)) return false;

        return true;
    }

    @Override
    protected boolean finalTradePreparation(Trade trade) {
        if (!super.finalTradePreparation(trade)) return false;

        SKRegularAdminShopkeeper shopkeeper = this.getShopkeeper();
        Player tradingPlayer = trade.getTradingPlayer();
        TradingRecipe tradingRecipe = trade.getTradingRecipe();

        // Find offer:
        UnmodifiableItemStack resultItem = tradingRecipe.getResultItem();
        TradeOffer offer = shopkeeper.getOffer(tradingRecipe);
        if (offer == null) {
            // Unexpected, because the recipes were created based on the shopkeeper's offers.
            TextUtils.sendMessage(tradingPlayer, Messages.cannotTradeUnexpectedTrade);
            this.debugPreventedTrade(
                    tradingPlayer,
                    "Could not find the offer corresponding to the trading recipe!"
            );
            return false;
        }

        if(offer.getStock() < resultItem.getAmount()) {
            TextUtils.sendMessage(tradingPlayer, Messages.cannotTradeUnexpectedTrade);
            this.debugPreventedTrade(
                    tradingPlayer,
                    "Not enough stock for the offer!"
            );
            return false;
        }

        offer.setStock(offer.getStock() - resultItem.getAmount());

        Bukkit.broadcastMessage("Stock: " + offer.getStock());

        return true;
    }

    @Override
    protected void onTradeOver(TradingContext tradingContext) {
        super.onTradeOver(tradingContext);

    }
}
