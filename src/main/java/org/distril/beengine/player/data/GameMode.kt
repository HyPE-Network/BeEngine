package org.distril.beengine.player.data

import com.nukkitx.protocol.bedrock.data.GameType

enum class GameMode(
	val identifier: String,
	val alias: String,
	val type: GameType
) {

	SURVIVAL("Survival", "s", GameType.SURVIVAL),
	CREATIVE("Creative", "c", GameType.CREATIVE),
	ADVENTURE("Adventure", "a", GameType.ADVENTURE),
	SPECTATOR("Spectator", "sp", GameType.SPECTATOR);

	companion object {

		fun fromIdentifierOrAlias(idOrAlias: String) = GameMode.values().firstOrNull {
			it.identifier.equals(idOrAlias, true) || it.alias.equals(idOrAlias, true)
		}

		fun fromId(id: Int) = GameMode.values()[id]
	}
}
