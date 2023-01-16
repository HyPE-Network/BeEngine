package org.distril.beengine.entity;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.packet.AddEntityPacket;
import com.nukkitx.protocol.bedrock.packet.RemoveEntityPacket;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.player.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Setter
@Getter
public class Entity {

	private static final AtomicLong ID = new AtomicLong(0);

	private final Set<Player> viewers = new HashSet<>();

	private final EntityType type;
	private final long id;

	private float pitch, yaw, headYaw;
	private Vector3f position = Vector3f.ZERO;

	private float maxHealth = 20f, health = 20f;

	private boolean spawned;

	public Entity(EntityType type) {
		this.type = type;
		this.id = ID.incrementAndGet();
	}

	public void onUpdate(long currentTick) {/**/}

	public void spawnTo(Player player) {
		if (this.viewers.add(player)) {
			player.sendPacket(this.createSpawnPacket(player));
		}
	}

	protected BedrockPacket createSpawnPacket(Player pLayer) {
		var packet = new AddEntityPacket();
		packet.setUniqueEntityId(this.getId());
		packet.setRuntimeEntityId(this.getId());
		packet.setIdentifier(this.type.getIdentifier());
		packet.setPosition(this.position);
		packet.setMotion(Vector3f.ZERO);
		packet.setRotation(Vector3f.from(this.getPitch(), this.getYaw(), this.getHeadYaw()));

		return packet;
	}

	public void despawnFrom(Player player) {
		if (this.viewers.remove(player)) {
			var packet = new RemoveEntityPacket();
			packet.setUniqueEntityId(this.getId());

			player.sendPacket(packet);
		}
	}

	public void despawnFromAll() {
		this.viewers.forEach(this::despawnFrom);
	}

	public Set<Player> getViewers() {
		return Collections.unmodifiableSet(this.viewers);
	}
}
