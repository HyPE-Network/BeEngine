package org.distril.beengine.player.data.attribute;

import com.nukkitx.protocol.bedrock.data.AttributeData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class Attribute {

	private final Type type;
	private float minValue, maxValue;
	private float defaultValue;
	private float value;

	public Attribute(Type type, float minValue, float maxValue, float defaultValue) {
		this.type = type;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.defaultValue = this.value = defaultValue;
	}

	public Attribute setMinValue(float minValue) {
		if (minValue > this.maxValue) {
			minValue = this.maxValue;
		}

		this.minValue = minValue;
		return this;
	}

	public Attribute setMaxValue(float maxValue) {
		if (maxValue < this.minValue) {
			maxValue = this.minValue;
		}

		this.maxValue = maxValue;
		return this;
	}

	public Attribute setDefaultValue(float defaultValue) {
		if (defaultValue > this.maxValue) {
			defaultValue = this.maxValue;
		} else if (defaultValue < this.minValue) {
			defaultValue = this.minValue;
		}

		this.defaultValue = defaultValue;
		return this;
	}

	public Attribute setValue(float value) {
		if (value > this.maxValue) {
			value = this.maxValue;
		} else if (value < this.minValue) {
			value = this.minValue;
		}

		this.value = value;
		return this;
	}

	public AttributeData toNetwork() {
		return new AttributeData(this.type.getName(), this.minValue, this.maxValue, this.value, this.defaultValue);
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	public Attribute clone() {
		return new Attribute(this.type, this.minValue, this.maxValue, this.defaultValue).setValue(this.value);
	}

	@Getter
	@AllArgsConstructor
	public enum Type {

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

		private final String name;
	}
}
