package org.distril.beengine.player.data.provider

import org.distril.beengine.player.data.PlayerData
import java.util.*

interface PlayerDataProvider {

	fun save(uuid: UUID, data: PlayerData, handler: (Throwable?) -> Unit)

	fun load(uuid: UUID, handler: (PlayerData, Throwable?) -> Unit)
}
