package org.distril.beengine.player.data;

import com.nukkitx.protocol.bedrock.data.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GameMode {

	SURVIVAL("Survival", "s", GameType.SURVIVAL),
	CREATIVE("Creative", "c", GameType.CREATIVE),
	ADVENTURE("Adventure", "a", GameType.ADVENTURE),
	SPECTATOR("Spectator", "", GameType.SPECTATOR);

	private final String identifier, alias;
	private final GameType type;

	public static GameMode fromIdentifierOrAlias(String idOrAlias) {
		for (GameMode gamemode : GameMode.values()) {
			if (gamemode.getIdentifier().equalsIgnoreCase(idOrAlias) || gamemode.getAlias().equalsIgnoreCase(idOrAlias)) {
				return gamemode;
			}
		}

		return null;
	}

	public static GameMode fromId(int id) {
		try {
			return GameMode.values()[id];
		} catch (ArrayIndexOutOfBoundsException exception) {
			return null;
		}
	}
}
