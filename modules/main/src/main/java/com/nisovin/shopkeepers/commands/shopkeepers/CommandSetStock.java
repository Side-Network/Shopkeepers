package com.nisovin.shopkeepers.commands.shopkeepers;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import com.nisovin.shopkeepers.api.ui.DefaultUITypes;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperArgument;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperFilter;
import com.nisovin.shopkeepers.commands.arguments.TargetShopkeeperFallback;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.arguments.BoundedIntegerArgument;
import com.nisovin.shopkeepers.commands.lib.commands.PlayerCommand;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.admin.regular.SKRegularAdminShopkeeper;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.IntStream;

class CommandSetStock extends PlayerCommand {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";
	private static final String ARGUMENT_OFFER = "offer";
	private static final String ARGUMENT_STOCK = "stock";


	CommandSetStock() {
		super("setstock");

		// Set permission:
		this.setPermission(ShopkeepersPlugin.REMOTE_EDIT_PERMISSION);

		// Set description:
		this.setDescription(Messages.commandDescriptionRemoteEdit);

		// Arguments:
		this.addArgument(new TargetShopkeeperFallback(
				new ShopkeeperArgument(ARGUMENT_SHOPKEEPER,
						ShopkeeperFilter.withAccess(DefaultUITypes.EDITOR())),
				TargetShopkeeperFilter.ADMIN
		));

		this.addArgument(new BoundedIntegerArgument(ARGUMENT_OFFER, 0, 1024).orDefaultValue(0));

		this.addArgument(new BoundedIntegerArgument(ARGUMENT_STOCK, 0, 2147483647).orDefaultValue(0));
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		assert (input.getSender() instanceof Player);
		Player player = (Player) input.getSender();

		SKRegularAdminShopkeeper shopkeeper = context.get(ARGUMENT_SHOPKEEPER);
		int offerIndex = context.get(ARGUMENT_OFFER);
		int stock = context.get(ARGUMENT_STOCK);

		List<? extends TradeOffer> offers = shopkeeper.getOffers();

		// check if offer exists

		if (offerIndex < 0 || offerIndex >= offers.size()) {
			TextUtils.sendMessage(player, "offer not found");
			return;
		}

		if (stock < 0) {
			TextUtils.sendMessage(player, "stock cannot be negative");
			return;
		}

		TradeOffer offer = offers.get(offerIndex);

		if (offer == null) {
			TextUtils.sendMessage(player, "offer is null");
			return;
		}

		int currentStock = offer.getStock();
		offer.setStock(stock);
		TextUtils.sendMessage(player, "Offer - " + offerIndex + " Set stock of offer " + offerIndex + " from " + currentStock + " to " + stock);

		shopkeeper.save();
	}
}
