package org.distril.beengine.entity;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import com.nukkitx.protocol.bedrock.data.entity.EntityDataMap;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import com.nukkitx.protocol.bedrock.packet.AddEntityPacket;
import com.nukkitx.protocol.bedrock.packet.RemoveEntityPacket;
import com.nukkitx.protocol.bedrock.packet.SetEntityDataPacket;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.entity.data.EntityMetadata;
import org.distril.beengine.player.Player;
import org.distril.beengine.server.Server;
import org.distril.beengine.world.World;
import org.distril.beengine.world.chunk.Chunk;
import org.distril.beengine.world.util.Location;

import java.io.Closeable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Setter
@Getter
public abstract class Entity implements Closeable {

	private static final AtomicLong NEXT_ID = new AtomicLong(0);

	private final Set<Player> viewers = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private final EntityType type;
	private final long id;

	private final EntityMetadata metadata = new EntityMetadata(this::onDataChange);

	private float pitch, yaw;
	private Location location;

	private float maxHealth = 20F, health = 20F;

	private boolean spawned;

	public Entity(EntityType type, Location location) {
		this.type = type;
		this.id = NEXT_ID.incrementAndGet();

		this.location = location;

		if (!(this instanceof Player)) {
			this.init();
		}
	}

	public void onUpdate(long currentTick) {
		this.metadata.update();
	}

	public abstract float getHeight();

	public abstract float getWidth();

	protected void init() {
		if (this.location == null) {
			throw new IllegalArgumentException("Invalid garbage Location given to Entity");
		}

		this.metadata.setFlag(EntityFlag.HAS_COLLISION, true);
		this.metadata.setShort(EntityData.AIR_SUPPLY, 400);
		this.metadata.setShort(EntityData.MAX_AIR_SUPPLY, 400);
		this.metadata.setLong(EntityData.LEASH_HOLDER_EID, -1L);
		this.metadata.setFloat(EntityData.SCALE, 1F);
		this.metadata.setFloat(EntityData.BOUNDING_BOX_HEIGHT, this.getHeight());
		this.metadata.setFloat(EntityData.BOUNDING_BOX_WIDTH, this.getWidth());
		this.metadata.setInt(EntityData.HEALTH, (int) this.getHealth());

		this.getWorld().addEntity(this, true);
	}

	private void onDataChange(EntityDataMap changeSet) {
		this.sendDataToViewers(changeSet);

		if (this instanceof Player player) {
			var packet = new SetEntityDataPacket();
			packet.setRuntimeEntityId(this.id);
			packet.getMetadata().putAll(changeSet);
			player.sendPacket(packet);
		}
	}

	private void sendDataToViewers(EntityDataMap dataMap) {
		var packet = new SetEntityDataPacket();
		packet.setRuntimeEntityId(this.id);
		packet.getMetadata().putAll(dataMap);

		Server.getInstance().broadcastPacket(this.getViewers(), packet);
	}

	public void sendData(Player player) {
		var packet = new SetEntityDataPacket();
		packet.setRuntimeEntityId(this.id);
		packet.getMetadata().putAll(this.metadata.getData());
		player.sendPacket(packet);
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

	public void setHealth(float health) {
		if (this.health == health) {
			return;
		}

		if (health < 1) {
			this.close();
		} else if (health <= this.maxHealth || health < this.health) {
			this.health = health;
		} else {
			this.health = this.maxHealth;
		}

		this.metadata.setInt(EntityData.HEALTH, (int) this.health);
	}

	@Override
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
