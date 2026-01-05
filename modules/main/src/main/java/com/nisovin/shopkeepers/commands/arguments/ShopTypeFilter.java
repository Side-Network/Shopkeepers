package com.nisovin.shopkeepers.commands.arguments;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.shopkeeper.ShopType;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopType;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopType;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.CommandArgument;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentFilter;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.java.Validate;

public final class ShopTypeFilter {

	public static final ArgumentFilter<ShopType<?>> ANY = ArgumentFilter.acceptAny();

	public static final ArgumentFilter<@Nullable ShopType<?>> ADMIN = new ArgumentFilter<@Nullable ShopType<?>>() {
		@Override
		public boolean test(
				CommandInput input,
				CommandContextView context,
				@Nullable ShopType<?> shopType
		) {
			return (shopType instanceof AdminShopType);
		}

		@Override
		public Text getInvalidArgumentErrorMsg(
				CommandArgument<?> argument,
				String argumentInput,
				@Nullable ShopType<?> value
		) {
			Validate.notNull(argumentInput, "argumentInput is null");
			Text text = Messages.commandShopTypeArgumentNoAdminShop;
			text.setPlaceholderArguments(argument.getDefaultErrorMsgArgs());
			text.setPlaceholderArguments("argument", argumentInput);
			return text;
		}
	};

	public static final ArgumentFilter<@Nullable ShopType<?>> PLAYER = new ArgumentFilter<@Nullable ShopType<?>>() {
		@Override
		public boolean test(
				CommandInput input,
				CommandContextView context,
				@Nullable ShopType<?> shopType
		) {
			return (shopType instanceof PlayerShopType);
		}

		@Override
		public Text getInvalidArgumentErrorMsg(
				CommandArgument<?> argument,
				String argumentInput,
				@Nullable ShopType<?> value
		) {
			Validate.notNull(argumentInput, "argumentInput is null");
			Text text = Messages.commandShopTypeArgumentNoPlayerShop;
			text.setPlaceholderArguments(argument.getDefaultErrorMsgArgs());
			text.setPlaceholderArguments("argument", argumentInput);
			return text;
		}
	};

	private ShopTypeFilter() {
	}
}
