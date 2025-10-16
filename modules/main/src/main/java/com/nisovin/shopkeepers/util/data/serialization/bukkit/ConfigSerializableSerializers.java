package com.nisovin.shopkeepers.util.data.serialization.bukkit;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.data.serialization.DataSerializer;
import com.nisovin.shopkeepers.util.data.serialization.InvalidDataException;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * {@link DataSerializer}s for {@link ConfigurationSerializable} values.
 */
public final class ConfigSerializableSerializers {

	private static class ConfigSerializableSerializer<C extends ConfigurationSerializable>
			implements DataSerializer<C> {

		protected final Class<@NonNull C> valueType;

		/**
		 * Creates a new {@link ConfigSerializableSerializer}.
		 * 
		 * @param valueType
		 *            the value class, not <code>null</code>
		 */
		public ConfigSerializableSerializer(Class<@NonNull C> valueType) {
			Validate.notNull(valueType, "valueType is null");
			this.valueType = valueType;
		}

		@Override
		public @Nullable Object serialize(C value) {
			Validate.notNull(value, "value is null");
			return value;
		}

		@Override
		public C deserialize(Object data) throws InvalidDataException {
			if (valueType.isInstance(data)) {
				return valueType.cast(data);
			}

			throw new InvalidDataException("Data is not of type '" + valueType.getName()
					+ "' but '" + data.getClass().getName() + "'!");
		}
	}

	/**
	 * Gets a {@link DataSerializer} for values of the specified type.
	 * 
	 * @param <C>
	 *            the value type
	 * @param valueType
	 *            the value class, not <code>null</code>
	 * @return the data serializer, not <code>null</code>
	 */
	public static <C extends ConfigurationSerializable> DataSerializer<@NonNull C> strict(
			Class<@NonNull C> valueType
	) {
		return new ConfigSerializableSerializer<@NonNull C>(valueType);
	}

	private ConfigSerializableSerializers() {
	}
}
