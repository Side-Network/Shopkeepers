package com.nisovin.shopkeepers.api.types;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link TypeRegistry} that keeps track of {@link SelectableType SelectableTypes} and the
 * selections of players.
 *
 * @param <T>
 *            the type of {@link SelectableType} that is managed by this registry
 */
public interface SelectableTypeRegistry<T extends SelectableType> extends TypeRegistry<T> {

	// SELECTION MANAGEMENT

	/**
	 * Checks if the specified player can select the specified type.
	 * <p>
	 * This may for example check that the type is {@link Type#isEnabled() enabled} and that the
	 * player has the necessary {@link Type#getPermission() permission} to use it.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 * @param type
	 *            the type
	 * @return <code>true</code> if the player can select the specified type
	 */
	public boolean canBeSelected(Player player, @NonNull T type);

	/**
	 * Gets the first selectable type for the given player, starting at the default one.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 * @return the first selectable type for the given player, or <code>null</code> if the player
	 *         cannot select any of the registered types
	 */
	public @Nullable T getDefaultSelection(Player player);

	/**
	 * Gets the first selectable type for the given player, starting at the currently selected one.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 * @return the first selectable type for the given player, or <code>null</code> if the player
	 *         cannot select any of the registered types
	 */
	public @Nullable T getSelection(Player player);

	/**
	 * Modifies the given player's current selection to select the next type.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 * @return the player's new selection, or <code>null</code> if the player cannot select any of
	 *         the registered types
	 */
	public @Nullable T selectNext(Player player);

	/**
	 * Modifies the given player's current selection to select the previous type.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 * @return the player's new selection, or <code>null</code> if the player cannot select any of
	 *         the registered types
	 */
	public @Nullable T selectPrevious(Player player);

	/**
	 * Clears the given player's current selection.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 */
	public void clearSelection(Player player);

	/**
	 * Clears the current selections of all players.
	 */
	public void clearAllSelections();
}
