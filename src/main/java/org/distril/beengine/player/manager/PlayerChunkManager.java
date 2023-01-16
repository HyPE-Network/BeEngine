package org.distril.beengine.player.manager;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;
import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;
import it.unimi.dsi.fastutil.longs.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.entity.Entity;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.ChunkUtil;

@Getter
@Log4j2
public class PlayerChunkManager {

	private final Player player;

	private final LongComparator comparator;

	private final LongSet loadedChunks = new LongOpenHashSet();
	private final Long2ObjectMap<LevelChunkPacket> sendQueue = new Long2ObjectOpenHashMap<>();

	private int radius = 6;

	public PlayerChunkManager(Player player) {
		this.player = player;
		this.comparator = (o1, o2) -> {
			int x1 = ChunkUtil.fromKeyX(o1);
			int z1 = ChunkUtil.fromKeyZ(o1);
			int x2 = ChunkUtil.fromKeyX(o2);
			int z2 = ChunkUtil.fromKeyZ(o2);
			int spawnX = this.player.getPosition().getFloorX() >> 4;
			int spawnZ = this.player.getPosition().getFloorZ() >> 4;

			return Integer.compare(
					PlayerChunkManager.distance(spawnX, spawnZ, x1, z1),
					PlayerChunkManager.distance(spawnX, spawnZ, x2, z2));
		};
	}

	private static int distance(int centerX, int centerZ, int x, int z) {
		int dx = centerX - x;
		int dz = centerZ - z;
		return dx * dx + dz * dz;
	}

	public void setRadius(int radius) {
		if (this.radius != radius) {
			this.radius = Math.min(radius, this.player.getServer().getSettings().getChunkRadius());

			var packet = new ChunkRadiusUpdatedPacket();
			packet.setRadius(radius >> 4);
			this.player.sendPacket(packet);

			this.queueNewChunks();
		}
	}

	public synchronized void sendQueued() {
		var sendQueueIterator = this.sendQueue.long2ObjectEntrySet().iterator();
		// Remove chunks which are out of range
		while (sendQueueIterator.hasNext()) {
			var entry = sendQueueIterator.next();
			var key = entry.getLongKey();
			if (!this.loadedChunks.contains(key)) {
				sendQueueIterator.remove();
			}
		}

		LongList list = new LongArrayList(this.sendQueue.keySet());

		list.unstableSort(this.comparator);

		for (long key : list.toLongArray()) {
			var packet = this.sendQueue.get(key);
			if (packet == null) {
				break;
			}

			this.sendQueue.remove(key);
			this.player.sendPacket(packet);

			var chunk = this.player.getWorld().getChunkManager().getChunk(key);

			// Spawn entities
			for (Entity entity : chunk.getEntities().values()) {
				if (entity != this.player && !entity.isSpawned()) {
					entity.spawnTo(this.player);
				}
			}
		}
	}

	public void queueNewChunks() {
		this.queueNewChunks(this.player.getPosition());
	}

	public void queueNewChunks(Vector3f position) {
		this.queueNewChunks(position.getFloorX() >> 4, position.getFloorZ() >> 4);
	}

	public synchronized void queueNewChunks(int chunkX, int chunkZ) {
		/*LongSet chunksForRadius = new LongOpenHashSet();

		LongSet sentCopy = new LongOpenHashSet(this.loadedChunks);

		LongList chunksToLoad = new LongArrayList();*/

		for (int x = -this.radius; x <= this.radius; ++x) {
			for (int z = -this.radius; z <= this.radius; ++z) {
				int cx = chunkX + x;
				int cz = chunkZ + z;

				var key = ChunkUtil.key(cx, cz);

				var chunk = this.player.getWorld().getChunkManager().getChunk(cx, cz);

				player.sendPacket(chunk.createPacket());

				var packet = new NetworkChunkPublisherUpdatePacket();
				packet.setPosition(this.player.getPosition().toInt());
				packet.setRadius(this.radius * 16);

				this.player.sendPacket(packet);


				/*chunksForRadius.add(key);
				if (this.loadedChunks.add(key)) {
					chunksToLoad.add(key);
				}*/
			}
		}

		/*var loadedChunksChanged = this.loadedChunks.retainAll(chunksForRadius);
		if (loadedChunksChanged || !chunksToLoad.isEmpty()) {
			var packet = new NetworkChunkPublisherUpdatePacket();
			packet.setPosition(this.player.getPosition().toInt());
			packet.setRadius(this.radius);

			this.player.sendPacket(packet);
		}

		// Order chunks for smoother loading
		chunksToLoad.sort(this.comparator);

		for (long key : chunksToLoad.toLongArray()) {
			int cx = ChunkUtil.fromKeyX(key);
			int cz = ChunkUtil.fromKeyZ(key);

			if (this.sendQueue.putIfAbsent(key, null) == null) {
				this.player.getWorld().getChunkManager().generateChunk(cx, cz).thenApply(Chunk::createPacket).whenComplete((packet, throwable) -> {
					synchronized (PlayerChunkManager.this) {
						if (throwable != null) {
							if (this.sendQueue.remove(key, null)) {
								this.loadedChunks.remove(key);
							}

							log.error("Unable to create chunk packet for " + this.player.getName(), throwable);
						} else if (!this.sendQueue.replace(key, null, packet)) {
							// The chunk was already loaded!?
							if (this.sendQueue.containsKey(key)) {
								log.warn("Chunk ({},{}) already loaded for {}, value {}", cx, cz, this.player.getName(), this.sendQueue.get(key));
							}
						}
					}
				});
			}
		}

		sentCopy.removeAll(chunksForRadius);*/
	}
}
