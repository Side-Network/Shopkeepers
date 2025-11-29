package com.nisovin.shopkeepers.commands.lib.argument.filter;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.commands.lib.argument.CommandArgument;
import com.nisovin.shopkeepers.commands.lib.argument.InvalidArgumentException;
import com.nisovin.shopkeepers.commands.lib.arguments.FirstOfArgument;
import com.nisovin.shopkeepers.text.Text;

/**
 * An {@link InvalidArgumentException} that indicates that a parsed argument got rejected, for
 * example by an {@link ArgumentFilter} or because the input was ambiguous.
 * <p>
 * This type of argument exception results in the command argument parsing to be aborted early,
 * without pending fallbacks being evaluated: This argument exception indicates that the argument
 * was successfully parsed, but the parsed value was then rejected for some reason. Evaluating
 * fallbacks for this or earlier command arguments likely results in a less relevant argument
 * binding or parsing error down the line.
 * <p>
 * Example: Consider the trading history command "/history [online player name|fallback: any
 * name|fallback: self] [shop name|fallback: 'all']" and the user input "/history test": If no
 * online player with name "test" is found, we first check if the name matches a shop name, before
 * we fallback to accept it as an arbitrary player name input (which might then for example search
 * the database with this arbitrary player name). And if a shop with this name is found, the player
 * argument instead falls back to filter the history results for the executing player. However, if
 * there are multiple shops with this name and we throw an {@link ArgumentRejectedException} to
 * indicate this ambiguity, we prefer to abort the command parsing with this ambiguity error message
 * rather than evaluating it with the arbitrary player name input and then likely finding no result.
 * I.e. the {@link ArgumentRejectedException} indicates that the argument was indeed parsed
 * successfully as a shop name and should therefore bind as such, but the command execution should
 * subsequently fail due to the argument value being ambiguous.
 * <p>
 * Rather then aborting the command execution early, there are the following alternatives to
 * handling this situations which either result in a less relevant argument binding/error, or
 * redundant parsing effort:
 * <ul>
 * <li>Evaluate fallbacks for this and/or previous command arguments: As shown in the example, this
 * likely results in less relevant argument bindings or parsing errors shown to the user. Fallbacks
 * of earlier command arguments have the semantic of "retry the parsing unless the argument is
 * parsed by one of the following arguments" (otherwise the argument would not use a fallback, but
 * an approach like {@link FirstOfArgument}). {@link ArgumentRejectedException} indicates that the
 * argument was indeed parsed by the subsequent command argument(s), so this binding should remain
 * intact. And continuing the parsing/fallbacks with the argument consumed is most often redundant,
 * as explained by the following point.
 * <li>Handle this more similarly to other successful argument parsing, i.e. continue the parsing
 * beyond this argument: If the parsing of the following command argument succeeds, the command is
 * meant to still fail with the error of the {@link ArgumentRejectedException}. And if the parsing
 * fails, it makes more sense to show the user the earlier encountered
 * {@link ArgumentRejectedException} rather then the parsing error for later command arguments. In
 * either case, there is no benefit to parse the remaining command arguments, since we prefer to
 * show the user the error of the {@link ArgumentRejectedException} anyway.
 * </ul>
 * TODO One potential reason for actually continuing the parsing and subsequent evaluation of
 * earlier fallbacks would be to check if one of the earlier command argument fallbacks eventually
 * fails, in which case it might be preferred to show its fallback error to the user instead. In the
 * example above, if the "self" fallback fails because the command is invoked from the console
 * instead of by a player, it might be preferred to show the "not a player" error to the user
 * instead. However, this is a rather minor issue currently, and simply aborting early when an
 * {@link ArgumentRejectedException} is encountered is far easier implemention-wise than having to
 * backtrack and still preserve the consumed arguments from later command arguments.
 */
public class ArgumentRejectedException extends InvalidArgumentException {

	private static final long serialVersionUID = 7271352558586958559L;

	public ArgumentRejectedException(CommandArgument<?> argument, Text message) {
		this(argument, message, null);
	}

	public ArgumentRejectedException(
			CommandArgument<?> argument,
			Text message,
			@Nullable Throwable cause
	) {
		super(argument, message, cause);
	}
}
