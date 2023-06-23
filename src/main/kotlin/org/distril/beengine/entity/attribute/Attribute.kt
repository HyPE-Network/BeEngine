package org.distril.beengine.entity.attribute

import com.nukkitx.protocol.bedrock.data.AttributeData
import kotlin.math.max
import kotlin.math.min

class Attribute(
	val type: Type,
	minValue: Float,
	maxValue: Float,
	defaultValue: Float
) {

	var minValue: Float = minValue
		private set
	var maxValue: Float = maxValue
		private set
	var defaultValue: Float = defaultValue
		private set
	var value: Float = defaultValue
		private set

	fun minValue(minValue: Float): Attribute {
		this.minValue = min(this.maxValue, max(this.maxValue, minValue))
		return this
	}

	fun maxValue(maxValue: Float): Attribute {
		this.maxValue = max(this.minValue, maxValue)

		this.defaultValue(this.defaultValue)
		this.value(this.value)
		return this
	}

	fun defaultValue(defaultValue: Float): Attribute {
		this.defaultValue = max(this.minValue, min(this.maxValue, defaultValue))
		return this
	}

	fun value(value: Float): Attribute {
		this.value = max(this.minValue, min(this.maxValue, value))
		return this
	}

	fun toNetwork() = AttributeData(this.type.networkId, this.minValue, this.maxValue, this.value, this.defaultValue)

	fun clone() = Attribute(this.type, this.minValue, this.maxValue, this.defaultValue).value(this.value)

	enum class Type(val networkId: String) {

		ABSORPTION("minecraft:absorption"),
		SATURATION("minecraft:player.saturation"),
		EXHAUSTION("minecraft:player.exhaustion"),
		KNOCKBACK_RESISTANCE("minecraft:knockback_resistance"),
		HEALTH("minecraft:health"),
		MOVEMENT_SPEED("minecraft:movement"),
		FOLLOW_RANGE("minecraft:follow_range"),
		HUNGER("minecraft:player.hunger"),
		ATTACK_DAMAGE("minecraft:attack_damage"),
		LEVEL("minecraft:player.level"),
		EXPERIENCE("minecraft:player.experience"),
		LUCK("minecraft:luck");
	}
}
