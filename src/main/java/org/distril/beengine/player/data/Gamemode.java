package org.distril.beengine.player.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gamemode {

	SURVIVAL("Survival", "s"),
	CREATIVE("Creative", "c"),
	ADVENTURE("Adventure", "a"),
	SPECTATOR("Spectator", "sp");

	private final String identifier, alias;
}
