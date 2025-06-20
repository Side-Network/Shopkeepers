package com.nisovin.shopkeepers.compat;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.java.Validate;

public class CompatVersion {

	public static final String VARIANT_PAPER = "paper";

	private static String getVariant(String compatVersion) {
		// Expected version format: "1_21_R5_some_variant_tag"
		var variantSeparatorIndex = compatVersion.indexOf('_', 7);
		if (variantSeparatorIndex < 0) return "";

		return compatVersion.substring(variantSeparatorIndex + 1);
	}

	private final String compatVersion;
	private final String variant;
	private final String minecraftVersion;
	private final String mappingsVersion;

	public CompatVersion(String compatVersion, String minecraftVersion, String mappingsVersion) {
		Validate.notEmpty(compatVersion, "compatVersion is empty");
		Validate.notEmpty(minecraftVersion, "minecraftVersion is empty");
		Validate.notEmpty(mappingsVersion, "mappingsVersion is empty");
		this.compatVersion = compatVersion;
		this.variant = getVariant(compatVersion);
		this.minecraftVersion = minecraftVersion;
		this.mappingsVersion = mappingsVersion;
	}

	/**
	 * Gets the Shopkeepers compatibility version.
	 * 
	 * @return the Shopkeepers compatibility version
	 */
	public String getCompatVersion() {
		return compatVersion;
	}

	/**
	 * Gets the variant component of the compatibility version, e.g. {@link #VARIANT_PAPER}.
	 * 
	 * @return the variant component of the compatibility version, or an empty String if there is no
	 *         variant
	 */
	public String getVariant() {
		return variant;
	}

	/**
	 * Checks whether this compatibility version has a non-empty {@link #getVariant() variant}.
	 * 
	 * @return <code>true</code> if this compatibility version has a non-empty variant
	 */
	public boolean hasVariant() {
		return !this.getVariant().isEmpty();
	}

	/**
	 * Gets the Minecraft server version.
	 * 
	 * @return the Minecraft server version
	 */
	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	/**
	 * Gets the server mappings version.
	 * <p>
	 * Note: On Paper, since 1.21.6, the server no longer supports the mappings version, so this
	 * returns the Minecraft version instead.
	 * 
	 * @return the server mappings version
	 */
	public String getMappingsVersion() {
		return mappingsVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + compatVersion.hashCode();
		result = prime * result + minecraftVersion.hashCode();
		result = prime * result + mappingsVersion.hashCode();
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CompatVersion)) return false;
		CompatVersion other = (CompatVersion) obj;
		if (!compatVersion.equals(other.compatVersion)) return false;
		if (!mappingsVersion.equals(other.mappingsVersion)) return false;
		if (!minecraftVersion.equals(other.minecraftVersion)) return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CompatVersion [compatVersion=");
		builder.append(compatVersion);
		builder.append(", minecraftVersion=");
		builder.append(minecraftVersion);
		builder.append(", mappingsVersion=");
		builder.append(mappingsVersion);
		builder.append("]");
		return builder.toString();
	}
}
