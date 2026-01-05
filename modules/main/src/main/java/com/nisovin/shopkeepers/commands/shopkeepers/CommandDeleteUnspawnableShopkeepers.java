package com.nisovin.shopkeepers.commands.shopkeepers;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.commands.Confirmations;
import com.nisovin.shopkeepers.commands.lib.Command;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.registry.SKShopkeeperRegistry;
import com.nisovin.shopkeepers.shopobjects.AbstractShopObject;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

class CommandDeleteUnspawnableShopkeepers extends Command {

	private static final int MAX_LIST_COUNT = 5;

	private final SKShopkeeperRegistry shopkeeperRegistry;
	private final Confirmations confirmations;

	CommandDeleteUnspawnableShopkeepers(
			SKShopkeeperRegistry shopkeeperRegistry,
			Confirmations confirmations
	) {
		super("deleteUnspawnableShopkeepers");

		this.shopkeeperRegistry = shopkeeperRegistry;
		this.confirmations = confirmations;

		// Set permission:
		this.setPermission(ShopkeepersPlugin.DELETE_UNSPAWNABLE_SHOPKEEPERS);

		// Set description:
		this.setDescription(Text.of("Deletes shopkeepers that failed to spawn."));

		// Hidden utility command:
		this.setHiddenInParentHelp(true);
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		CommandSender sender = input.getSender();

		// Check for shopkeepers that failed to spawn (without deleting them):
		var spawner = shopkeeperRegistry.getShopkeeperSpawner();
		var unspawnableShopkeepers = spawner.checkUnspawnableShopkeepers(false, true);
		if (unspawnableShopkeepers.isEmpty()) {
			sender.sendMessage(ChatColor.GREEN + "There are no shopkeepers that failed to spawn "
					+ "during their last spawn attempt!");
			return;
		}

		TextUtils.sendMessage(sender, ChatColor.RED + "Found " + ChatColor.YELLOW
				+ unspawnableShopkeepers.size() + ChatColor.RED + " shopkeepers that failed to "
				+ "spawn during their last spawn attempt:");
		var listedCount = 0;
		for (var shopkeeper : unspawnableShopkeepers) {
			listedCount += 1;
			if (listedCount > MAX_LIST_COUNT) {
				TextUtils.sendMessage(sender, ChatColor.RED + "...");
				break;
			}

			TextUtils.sendMessage(sender, ChatColor.RED + "- " + ChatColor.YELLOW
					+ shopkeeper.getId() + ChatColor.RED + " at " + ChatColor.YELLOW
					+ shopkeeper.getPositionString());
		}

		// Dangerous: Ask for confirmation before deleting the shopkeepers.
		confirmations.awaitConfirmation(sender, () -> {
			var deleted = 0;
			for (var shopkeeper : unspawnableShopkeepers) {
				if (!shopkeeper.isValid()) {
					return;
				}

				var shopObject = (AbstractShopObject) shopkeeper.getShopObject();
				if (!shopObject.isLastSpawnFailed()) {
					return;
				}

				shopkeeper.delete();
				deleted += 1;
			}

			SKShopkeepersPlugin.getInstance().getShopkeeperStorage().save();

			sender.sendMessage(ChatColor.GREEN + "Deleted " + ChatColor.YELLOW + deleted
					+ ChatColor.GREEN + " shopkeepers that failed to spawn during their last spawn "
					+ "attempt!");
		});

		TextUtils.sendMessage(sender, ChatColor.RED
				+ "Do you want to irrevocably delete these shopkeepers?");
		TextUtils.sendMessage(sender, Messages.confirmationRequired);
	}
}
