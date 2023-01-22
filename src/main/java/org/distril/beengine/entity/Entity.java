package org.distril.beengine.entity;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.packet.AddEntityPacket;
import com.nukkitx.protocol.bedrock.packet.RemoveEntityPacket;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.player.Player;
import org.distril.beengine.world.World;
import org.distril.beengine.world.chunk.Chunk;
import org.distril.beengine.world.util.Location;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Setter
@Getter
public class Entity {

	private static final AtomicLong NEXT_ID = new AtomicLong(0);

	private final Set<Player> viewers = new HashSet<>();

	private final EntityType type;
	private final long id;

	private float pitch, yaw;
	private Location location;

	private float maxHealth = 20f, health = 20f;

	private boolean spawned;

	public Entity(EntityType type, Location location) {
		this.type = type;
		this.id = NEXT_ID.incrementAndGet();

		this.location = location;

		this.init();
	}

	public void onUpdate(long currentTick) {/**/}

	protected void init() {
		if (this.location == null) {
			throw new IllegalArgumentException("Invalid garbage Location given to Entity");
		}

		this.getWorld().addEntity(this, true);
	}

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
		packet.setPosition(this.getPosition());
		packet.setMotion(Vector3f.ZERO);
		packet.setRotation(Vector3f.from(this.pitch, this.yaw, this.yaw));

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

	public World getWorld() {
		return this.location.getWorld();
	}

	public Chunk getChunk() {
		return this.location.getChunk();
	}

	public Vector3f getPosition() {
		return this.location.getPosition();
	}

	public void setPosition(Vector3f position) {
		this.location = Location.from(position, this.getWorld());
	}

	public void setRotation(float pitch, float yaw) {
		this.pitch = pitch;
		this.yaw = yaw;
	}

	public void close() {
		if (this.spawned) {
			this.spawned = false;

			this.despawnFromAll();

			if (this.getWorld() != null) {
				this.getWorld().removeEntity(this, true);
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		Entity that = (Entity) obj;
		return this.id == that.getId();
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}
}
