package org.distril.beengine.player.data.attribute

import com.nukkitx.protocol.bedrock.data.AttributeData

class Attribute(
	val type: Type,
	private var minValue: Float,
	private var maxValue: Float,
	private var defaultValue: Float
) {

	var value: Float = defaultValue

	fun minValue(minValue: Float): Attribute {
		var minValue = minValue
		if (minValue > this.maxValue) minValue = this.maxValue

		this.minValue = minValue
		return this
	}

	fun maxValue(maxValue: Float): Attribute {
		var maxValue = maxValue
		if (maxValue < this.minValue) maxValue = this.minValue

		this.maxValue = maxValue
		return this
	}

	fun defaultValue(defaultValue: Float): Attribute {
		var defaultValue = defaultValue
		if (defaultValue > this.maxValue) {
			defaultValue = this.maxValue
		} else if (defaultValue < this.minValue) {
			defaultValue = this.minValue
		}

		this.value = defaultValue
		return this
	}

	fun value(value: Float): Attribute {
		var value = value
		if (value > this.maxValue) {
			value = this.maxValue
		} else if (value < this.minValue) {
			value = this.minValue
		}

		this.value = value
		return this
	}

	fun toNetwork() = AttributeData(this.type.networkName, this.minValue, this.maxValue, this.value, this.defaultValue)

	fun clone() = Attribute(this.type, this.minValue, this.maxValue, this.defaultValue).value(this.value)

	enum class Type(val networkName: String) {

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
