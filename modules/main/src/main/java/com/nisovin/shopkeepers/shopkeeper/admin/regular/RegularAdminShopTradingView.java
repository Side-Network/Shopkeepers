package com.nisovin.shopkeepers.shopkeeper.admin.regular;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.trading.Trade;
import com.nisovin.shopkeepers.ui.trading.TradingView;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

public class RegularAdminShopTradingView extends TradingView {

	protected RegularAdminShopTradingView(
			RegularAdminShopTradingViewProvider provider,
			Player player,
			UIState uiState
	) {
		super(provider, player, uiState);
	}

	@Override
	public SKRegularAdminShopkeeper getShopkeeperNonNull() {
		return (SKRegularAdminShopkeeper) super.getShopkeeperNonNull();
	}

	@Override
	protected boolean prepareTrade(Trade trade) {
		if (!super.prepareTrade(trade)) return false;

		SKRegularAdminShopkeeper shopkeeper = this.getShopkeeperNonNull();
		Player tradingPlayer = trade.getTradingPlayer();
		TradingRecipe tradingRecipe = trade.getTradingRecipe();

		// Find offer:
		TradeOffer offer = shopkeeper.getOffer(tradingRecipe);
		if (offer == null) {
			// Unexpected, because the recipes were created based on the shopkeeper's offers.
			TextUtils.sendMessage(tradingPlayer, Messages.cannotTradeUnexpectedTrade);
			this.debugPreventedTrade("Could not find the offer corresponding to the trading recipe!");
			return false;
		}

		return true;
	}

	@Override
	protected boolean finalTradePreparation(Trade trade) {
		if (!super.finalTradePreparation(trade)) return false;

		SKRegularAdminShopkeeper shopkeeper = this.getShopkeeperNonNull();
		Player tradingPlayer = trade.getTradingPlayer();
		TradingRecipe tradingRecipe = trade.getTradingRecipe();

		// Find offer:
		UnmodifiableItemStack resultItem = tradingRecipe.getResultItem();
		@Nullable TradeOffer offer = shopkeeper.getOffer(tradingRecipe);
		if (offer == null) {
			// Unexpected, because the recipes were created based on the shopkeeper's offers.
			TextUtils.sendMessage(tradingPlayer, Messages.cannotTradeUnexpectedTrade);
			this.debugPreventedTrade("Could not find the offer corresponding to the trading recipe!");
			return false;
		}

		// Check if there is enough stock:
		assert resultItem != null;
		if (offer.getStock() < resultItem.getAmount()) {
			TextUtils.sendMessage(tradingPlayer, Messages.cannotTradeInsufficientStock);
			this.debugPreventedTrade("Not enough stock for the offer!");
			return false;
		}

		// Decrease the stock:
		offer.setStock(offer.getStock() - resultItem.getAmount());
		shopkeeper.updateOffer(offer);
		shopkeeper.save();

		return true;
	}
}
