package org.distril.beengine.entity

import com.nukkitx.protocol.bedrock.data.entity.EntityEventType
import com.nukkitx.protocol.bedrock.packet.EntityEventPacket
import org.distril.beengine.server.Server

abstract class EntityLiving(type: EntityType) : Entity(type) {

	override var health = super.health
		set(value) {
			val wasAlive = this.isAlive
			super.health = value
			if (this.isAlive && !wasAlive) {
				val packet = EntityEventPacket()
				packet.runtimeEntityId = this.id
				packet.type = EntityEventType.RESPAWN

				Server.broadcastPacket(this.viewers, packet)
			}

			field = value
		}

	override fun kill() {
		if (this.isAlive) {
			super.kill()

			// TODO: drop all items
		}
	}
}
