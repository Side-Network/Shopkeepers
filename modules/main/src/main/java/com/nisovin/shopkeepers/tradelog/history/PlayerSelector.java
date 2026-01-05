package com.nisovin.shopkeepers.tradelog.history;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.java.Validate;

public interface PlayerSelector {

	public static final PlayerSelector ALL = new PlayerSelector() {
		@Override
		public String toString() {
			return "PlayerSelector.ALL";
		}
	};

	public static class ByUUID implements PlayerSelector {

		private final UUID playerUUID;
		// The player name, if known:
		private final @Nullable String playerName;

		public ByUUID(UUID playerUUID, @Nullable String playerName) {
			Validate.notNull(playerUUID);
			this.playerUUID = playerUUID;
			this.playerName = playerName;
		}

		public UUID getPlayerUUID() {
			return playerUUID;
		}

		public @Nullable String getPlayerName() {
			return playerName;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PlayerSelector.ByUUID [playerUUID=");
			builder.append(playerUUID);
			builder.append(", playerName=");
			builder.append(playerName);
			builder.append("]");
			return builder.toString();
		}
	}
}
