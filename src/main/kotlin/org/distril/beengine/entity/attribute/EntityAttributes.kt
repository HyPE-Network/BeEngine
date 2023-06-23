package org.distril.beengine.entity.attribute

import com.nukkitx.protocol.bedrock.packet.UpdateAttributesPacket
import org.distril.beengine.player.Player
import java.util.*

class EntityAttributes {

	private val attributes: MutableMap<Attribute.Type, Attribute> = EnumMap(Attribute.Type::class.java)

	init {
		this.setAttribute(Attribute(Attribute.Type.HEALTH, 0f, 20f, 20f))
		this.setAttribute(Attribute(Attribute.Type.MOVEMENT_SPEED, 0f, Float.MAX_VALUE, 0.10f))
	}

	fun send(player: Player) {
		val packet = UpdateAttributesPacket()
		packet.runtimeEntityId = player.id
		packet.attributes.addAll(this.attributes.values.map { it.toNetwork() })

		player.sendPacket(packet)
	}

	fun setAttribute(attribute: Attribute) {
		this.attributes[attribute.type] = attribute
	}

	operator fun get(type: Attribute.Type) = this.attributes[type]!!
}
