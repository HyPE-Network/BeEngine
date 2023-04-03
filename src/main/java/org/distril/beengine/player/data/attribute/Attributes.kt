package org.distril.beengine.player.data.attribute

import com.nukkitx.protocol.bedrock.packet.UpdateAttributesPacket
import org.distril.beengine.player.Player
import java.util.*

class Attributes(private val player: Player) {

	private val attributes: MutableMap<Attribute.Type, Attribute> = EnumMap(Attribute.Type::class.java)

	init {
		this.attributes[Attribute.Type.ABSORPTION] =
			Attribute(Attribute.Type.ABSORPTION, 0f, Float.MAX_VALUE, 0f)
		this.attributes[Attribute.Type.SATURATION] =
			Attribute(Attribute.Type.SATURATION, 0f, 20f, 5f)
		this.attributes[Attribute.Type.EXHAUSTION] =
			Attribute(Attribute.Type.EXHAUSTION, 0f, 5f, 0.41f)
		this.attributes[Attribute.Type.KNOCKBACK_RESISTANCE] =
			Attribute(Attribute.Type.KNOCKBACK_RESISTANCE, 0f, 1f, 0f)
		this.attributes[Attribute.Type.HEALTH] =
			Attribute(Attribute.Type.HEALTH, 0f, 20f, 20f)
		this.attributes[Attribute.Type.MOVEMENT_SPEED] =
			Attribute(Attribute.Type.MOVEMENT_SPEED, 0f, Float.MAX_VALUE, 0.10f)
		this.attributes[Attribute.Type.FOLLOW_RANGE] =
			Attribute(Attribute.Type.FOLLOW_RANGE, 0f, 2048f, 16f)
		this.attributes[Attribute.Type.HUNGER] =
			Attribute(Attribute.Type.HUNGER, 0f, 20f, 20f)
		this.attributes[Attribute.Type.ATTACK_DAMAGE] =
			Attribute(Attribute.Type.ATTACK_DAMAGE, 0f, Float.MAX_VALUE, 1f)
		this.attributes[Attribute.Type.LEVEL] =
			Attribute(Attribute.Type.LEVEL, 0f, 24791f, 0f)
		this.attributes[Attribute.Type.EXPERIENCE] =
			Attribute(Attribute.Type.EXPERIENCE, 0f, 1f, 0f)
		this.attributes[Attribute.Type.LUCK] =
			Attribute(Attribute.Type.LUCK, -1024f, 1024f, 0f)
	}

	fun sendAll() {
		this.setAttribute(
			this[Attribute.Type.HEALTH]
				.maxValue(this.player.maxHealth)
				.value(this.player.health)
		)
		// todo: HUNGER
		// todo: MOVEMENT_SPEED
		// todo: LEVEL
		// todo: EXPERIENCE
		val packet = UpdateAttributesPacket()
		packet.runtimeEntityId = this.player.id
		packet.attributes.addAll(this.attributes.values.map { it.toNetwork() })

		this.player.sendPacket(packet)
	}

	fun setAttribute(attribute: Attribute) {
		this.attributes[attribute.type] = attribute
	}

	operator fun get(type: Attribute.Type) = this.attributes[type]!!.clone()
}
