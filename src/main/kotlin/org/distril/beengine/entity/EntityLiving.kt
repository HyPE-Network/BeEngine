package org.distril.beengine.entity

import com.nukkitx.protocol.bedrock.data.entity.EntityEventType
import com.nukkitx.protocol.bedrock.packet.EntityEventPacket
import org.distril.beengine.server.Server

abstract class EntityLiving(type: EntityType) : Entity(type) {

	override var health: Float
		get() = super.health
		set(value) {
			super.health = value

			if (!this.isAlive) {
				this.kill()

				val packet = EntityEventPacket()
				packet.runtimeEntityId = this.id
				packet.type = EntityEventType.RESPAWN

				Server.broadcastPacket(this.viewers, packet)
			}
		}

	fun kill() {
		if (!this.isAlive) return

		// TODO: drop all items
	}
}
