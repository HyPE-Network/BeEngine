package org.distril.beengine.player.data.attribute;

import com.nukkitx.protocol.bedrock.packet.UpdateAttributesPacket;
import org.distril.beengine.player.Player;

import java.util.EnumMap;
import java.util.Map;

public class Attributes {

	private final Map<Attribute.Type, Attribute> attributes = new EnumMap<>(Attribute.Type.class);

	private final Player player;

	public Attributes(Player player) {
		this.player = player;

		this.attributes.put(Attribute.Type.ABSORPTION, new Attribute(Attribute.Type.ABSORPTION, 0f, Float.MAX_VALUE, 0f));
		this.attributes.put(Attribute.Type.SATURATION, new Attribute(Attribute.Type.SATURATION, 0f, 20f, 5f));
		this.attributes.put(Attribute.Type.EXHAUSTION, new Attribute(Attribute.Type.EXHAUSTION, 0f, 5f, 0.41f));
		this.attributes.put(Attribute.Type.KNOCKBACK_RESISTANCE, new Attribute(Attribute.Type.KNOCKBACK_RESISTANCE, 0f, 1f, 0f));
		this.attributes.put(Attribute.Type.HEALTH, new Attribute(Attribute.Type.HEALTH, 0f, 20f, 20f));
		this.attributes.put(Attribute.Type.MOVEMENT_SPEED, new Attribute(Attribute.Type.MOVEMENT_SPEED, 0f, Float.MAX_VALUE, 0.10f));
		this.attributes.put(Attribute.Type.FOLLOW_RANGE, new Attribute(Attribute.Type.FOLLOW_RANGE, 0f, 2048f, 16f));
		this.attributes.put(Attribute.Type.HUNGER, new Attribute(Attribute.Type.HUNGER, 0f, 20f, 20f));
		this.attributes.put(Attribute.Type.ATTACK_DAMAGE, new Attribute(Attribute.Type.ATTACK_DAMAGE, 0f, Float.MAX_VALUE, 1f));
		this.attributes.put(Attribute.Type.LEVEL, new Attribute(Attribute.Type.LEVEL, 0f, 24791f, 0f));
		this.attributes.put(Attribute.Type.EXPERIENCE, new Attribute(Attribute.Type.EXPERIENCE, 0f, 1f, 0f));
		this.attributes.put(Attribute.Type.LUCK, new Attribute(Attribute.Type.LUCK, -1024f, 1024f, 0f));
	}

	public void sendAll() {
		this.setAttribute(this.getAttribute(Attribute.Type.HEALTH)
				.maxValue(this.player.getMaxHealth())
				.value(this.player.getHealth()));
		// todo: HUNGER
		// todo: MOVEMENT_SPEED
		// todo: LEVEL
		// todo: EXPERIENCE

		var packet = new UpdateAttributesPacket();
		packet.setRuntimeEntityId(this.player.getId());
		packet.getAttributes().addAll(this.attributes.values().stream().map(Attribute::toNetwork).toList());
		this.player.sendPacket(packet);
	}

	public void setAttribute(Attribute attribute) {
		this.attributes.put(attribute.getType(), attribute);
	}

	public Attribute getAttribute(Attribute.Type type) {
		return this.attributes.get(type).clone();
	}
}
