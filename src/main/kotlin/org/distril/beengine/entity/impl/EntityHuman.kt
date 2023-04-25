package org.distril.beengine.entity.impl

import com.nukkitx.math.vector.Vector3f
import com.nukkitx.protocol.bedrock.data.PlayerPermission
import com.nukkitx.protocol.bedrock.data.command.CommandPermission
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag
import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin
import com.nukkitx.protocol.bedrock.packet.AddPlayerPacket
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket
import com.nukkitx.protocol.bedrock.packet.PlayerSkinPacket
import org.distril.beengine.entity.EntityCreature
import org.distril.beengine.entity.EntityType
import org.distril.beengine.network.data.Device
import org.distril.beengine.player.Player
import org.distril.beengine.world.util.Location
import java.util.*

open class EntityHuman : EntityCreature(EntityType.HUMAN) {

	var username: String? = null
	var xuid: String? = null
	var uuid: UUID? = null
	open var skin: SerializedSkin? = null
		set(value) {
			field = value

			val packet = PlayerSkinPacket().apply {
				newSkinName = ""
				oldSkinName = ""
				uuid = uuid
				skin = skin
				isTrustedSkin = true
			}

			this.viewers.forEach { it.sendPacket(packet) }
		}

	var device: Device? = null
	override fun init(location: Location): Boolean {
		this.metadata.setFlag(EntityFlag.HAS_GRAVITY, true)

		return super.init(location)
	}

	override val height = 1.8f

	override val width = 0.6f

	override val eyeHeight = 1.62f

	val playerListEntry
		get() = PlayerListPacket.Entry(this.uuid).apply {
			this.entityId = id
			this.name = username
			this.skin = skin
			this.xuid = xuid
			this.platformChatId = ""
			this.buildPlatform = device!!.osId
		}

	override fun createSpawnPacket(player: Player): AddPlayerPacket {
		val packet = AddPlayerPacket()
		packet.uuid = this.uuid
		packet.username = this.username
		packet.uniqueEntityId = this.id
		packet.runtimeEntityId = packet.uniqueEntityId
		packet.position = this.position
		packet.motion = Vector3f.ZERO
		packet.rotation = Vector3f.from(this.pitch, this.yaw, this.yaw)
		packet.deviceId = ""
		packet.platformChatId = ""
		packet.commandPermission = CommandPermission.OPERATOR
		packet.playerPermission = PlayerPermission.OPERATOR
		return packet
	}
}
