package com.nisovin.shopkeepers.util.bukkit;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.nisovin.shopkeepers.compat.Compat;
import com.nisovin.shopkeepers.util.java.CollectionUtils;
import com.nisovin.shopkeepers.util.java.PredicateUtils;

public class RegistryUtils {

	// Spigot has introduced RegistryAware for some Keyed types and deprecated their getKey method
	// in favor of RegistryAware#getKeyOrThrow. But this interface was never ported to Paper, so we
	// cannot use RegistryAware to resolve the deprecation warning without breaking compatibility
	// with Paper.
	// However, these objects still implement Keyed as before and simply behave like
	// RegistryAware#getKeyOrThrow internally for both Spigot and Paper. So we can avoid the
	// deprecation warning by using this method to call Keyed#getKey instead of the type's specific
	// deprecated #getKey method.
	public static NamespacedKey getKeyOrThrow(Keyed keyed) {
		return keyed.getKey();
	}

	public static <T extends Keyed> List<@NonNull T> getValues(Registry<@NonNull T> registry) {
		return registry.stream().toList();
	}

	public static <T extends Keyed> List<NamespacedKey> getKeys(Registry<@NonNull T> registry) {
		return registry.stream().map(Keyed::getKey).toList();
	}

	public static <T extends Keyed> @NonNull T cycleKeyed(
			Class<T> type,
			@NonNull T current,
			boolean backwards
	) {
		return cycleKeyed(type, current, backwards, PredicateUtils.alwaysTrue());
	}

	// Cycled through all values but none got accepted: Returns current value.
	public static <T extends Keyed> @NonNull T cycleKeyed(
			Class<T> type,
			@NonNull T current,
			boolean backwards,
			Predicate<? super @NonNull T> predicate
	) {
		var registry = Compat.getProvider().getRegistry(type);
		// TODO Cache the registry values? Or use the iterator/stream directly to get the next
		// value.
		List<@NonNull T> values = getValues(registry);
		return CollectionUtils.cycleValue(values, current, backwards, predicate);
	}

	private RegistryUtils() {
	}
}
