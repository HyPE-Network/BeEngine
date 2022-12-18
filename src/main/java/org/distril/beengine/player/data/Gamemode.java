package org.distril.beengine.player.data;

import com.nukkitx.protocol.bedrock.data.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gamemode {

	SURVIVAL("Survival", "s", GameType.SURVIVAL),
	CREATIVE("Creative", "c", GameType.CREATIVE),
	ADVENTURE("Adventure", "a", GameType.ADVENTURE),
	SPECTATOR("Spectator", "sp", GameType.SPECTATOR);

	private final String identifier, alias;
	private final GameType type;
}
