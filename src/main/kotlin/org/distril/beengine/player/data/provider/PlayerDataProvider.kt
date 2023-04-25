package org.distril.beengine.player.data.provider

import org.distril.beengine.player.data.PlayerData
import java.util.*

interface PlayerDataProvider {

	fun save(uuid: UUID, data: PlayerData)

	fun load(uuid: UUID): PlayerData
}
