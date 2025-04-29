package com.nisovin.shopkeepers.commands.shopkeepers;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import com.nisovin.shopkeepers.api.ui.DefaultUITypes;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperArgument;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperFilter;
import com.nisovin.shopkeepers.commands.arguments.TargetShopkeeperFallback;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.commands.PlayerCommand;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.admin.regular.SKRegularAdminShopkeeper;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import org.bukkit.entity.Player;

import java.util.stream.IntStream;

class CommandStockInfo extends PlayerCommand {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";

	CommandStockInfo() {
		super("stockinfo");

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
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		assert (input.getSender() instanceof Player);
		Player player = (Player) input.getSender();

		SKRegularAdminShopkeeper shopkeeper = context.get(ARGUMENT_SHOPKEEPER);

		IntStream.range(0, shopkeeper.getOffers().size()).forEach(i -> {
			TradeOffer offer = shopkeeper.getOffers().get(i);
			TextUtils.sendMessage(player,
					"Offer " + (i) + " - Stock: " + offer.getStock());
		});
	}
}
