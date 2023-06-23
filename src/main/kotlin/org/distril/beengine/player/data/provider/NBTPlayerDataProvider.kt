package org.distril.beengine.player.data.provider

import com.nukkitx.math.vector.Vector3f
import com.nukkitx.nbt.NbtMap
import com.nukkitx.nbt.NbtUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.distril.beengine.player.data.GameMode
import org.distril.beengine.player.data.PlayerData
import org.distril.beengine.server.Server
import org.distril.beengine.util.ModuleScope
import org.distril.beengine.util.Utils.getLogger
import org.distril.beengine.world.util.Location
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.*

class NBTPlayerDataProvider : PlayerDataProvider {

	private val scope = ModuleScope("NBTPlayerDataProvider", dispatcher = Dispatchers.IO)

	override fun save(uuid: UUID, data: PlayerData, handler: (Throwable?) -> Unit) {
		val playerFile = resolvePlayerNBTFile(uuid)
		if (playerFile.exists() && !playerFile.delete()) {
			log.error("Failed to save data of $uuid. Failed to delete existing player data file")
		}

		this.scope.launch {
			var throwable: Throwable? = null
			try {
				NbtUtils.createWriter(FileOutputStream(playerFile)).use { it.writeTag(createPlayerSaveData(data)) }
			} catch (e: Throwable) {
				throwable = e
			}

			handler(throwable)
		}
	}

	override fun load(uuid: UUID, handler: (PlayerData, Throwable?) -> Unit) {
		this.scope.launch {
			val playerFile = resolvePlayerNBTFile(uuid)

			var playerData = PlayerData()
			var throwable: Throwable? = null
			if (playerFile.exists()) {
				try {
					NbtUtils.createReader(FileInputStream(playerFile)).use {
						playerData = readPlayerData(it.readTag() as NbtMap)
					}
				} catch (e: Throwable) {
					throwable = e
				}
			}

			handler(playerData, throwable)
		}
	}

	private fun resolvePlayerNBTFile(uuid: UUID) = Path.of("players", "$uuid.dat").toFile()

	private fun createPlayerSaveData(data: PlayerData): NbtMap {
		val location = data.location
		return NbtMap.builder()
			.putFloat("pitch", data.pitch)
			.putFloat("yaw", data.yaw)
			.putFloat("headYaw", data.headYaw)
			.putFloat("x", location.x)
			.putFloat("y", location.y)
			.putFloat("z", location.z)
			.putString("worldName", location.world.worldName)
			.putInt("gamemode", data.gameMode.ordinal)
			.build()
	}

	private fun readPlayerData(data: NbtMap): PlayerData {
		return PlayerData().apply {
			this.pitch = data.getFloat("pitch")
			this.yaw = data.getFloat("yaw")
			this.headYaw = data.getFloat("headYaw")

			val position = Vector3f.from(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"))
			val world = Server.worldRegistry.getWorld(data.getString("worldName"))!!
			this.location = Location(world, position)
			this.gameMode = GameMode.values()[data.getInt("gamemode")]
		}
	}


	companion object {

		private val log = NBTPlayerDataProvider.getLogger()
	}
}
