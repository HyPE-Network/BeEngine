package org.distril.beengine.player.manager;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;
import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;
import it.unimi.dsi.fastutil.longs.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.ChunkUtils;
import org.distril.beengine.world.chunk.Chunk;

import java.util.concurrent.atomic.AtomicLong;

@Getter
@Log4j2
public class PlayerChunkManager {

	private final Player player;

	private final AroundPlayerChunkComparator chunkComparator;

	private final LongConsumer removeChunkLoader;

	private final LongSet loadedChunks = new LongOpenHashSet();
	private final Long2ObjectMap<LevelChunkPacket> sendQueue = new Long2ObjectOpenHashMap<>();

	private final AtomicLong chunksSentCounter = new AtomicLong();

	private volatile int radius;

	public PlayerChunkManager(Player player) {
		this.player = player;
		this.chunkComparator = new AroundPlayerChunkComparator(player);

		this.removeChunkLoader = chunkKey -> {
			var chunk = player.getWorld().getLoadedChunk(chunkKey);
			if (chunk != null) {
				chunk.removeLoader(this.player);
				chunk.getEntities().forEach(entity -> entity.despawnFrom(this.player));
			}
		};
	}

	public void setRadius(int radius) {
		if (this.radius != radius) {
			this.radius = radius;

			var packet = new ChunkRadiusUpdatedPacket();
			packet.setRadius(radius >> 4);
			this.player.sendPacket(packet);

			this.queueNewChunks();
		}
	}

	public synchronized void sendQueued() {
		var chunksPerTick = this.player.getServer().getSettings().getChunksPerTick();

		var sendQueueIterator = this.sendQueue.long2ObjectEntrySet().iterator();
		// Remove chunks which are out of range
		while (sendQueueIterator.hasNext()) {
			var entry = sendQueueIterator.next();
			var key = entry.getLongKey();
			if (!this.loadedChunks.contains(key)) {
				sendQueueIterator.remove();

				var chunk = this.player.getWorld().getLoadedChunk(key);
				if (chunk != null) {
					chunk.removeLoader(this.player);
				}
			}
		}


		LongList list = new LongArrayList(this.sendQueue.keySet());

		// Order chunks around player.
		list.unstableSort(this.chunkComparator);

		for (var key : list.toLongArray()) {
			var packet = this.sendQueue.get(key);
			if (chunksPerTick <= 0 || packet == null) {
				break;
			}

			this.sendQueue.remove(key);

			this.player.sendPacket(packet);

			var chunk = this.player.getWorld().getChunkManager().getLoadedChunk(key);
			if (chunk == null) {
				log.warn("Attempted to send unloaded chunk ({}, {}) to {}",
						ChunkUtils.fromKeyX(key), ChunkUtils.fromKeyZ(key), this.player.getName());
				return;
			}

			// Spawn entities
			chunk.getEntities().forEach(entity -> {
				if (!entity.equals(this.player) && !entity.isSpawned()) {
					entity.spawnTo(this.player);
				}
			});

			chunksPerTick--;
			this.chunksSentCounter.incrementAndGet();
		}
	}

	public void queueNewChunks() {
		this.queueNewChunks(this.player.getPosition());
	}

	public void queueNewChunks(Vector3f position) {
		this.queueNewChunks(position.getFloorX() >> 4, position.getFloorZ() >> 4);
	}

	public synchronized void queueNewChunks(int chunkX, int chunkZ) {
		var radius = this.getChunkRadius();
		var radiusSqr = radius * radius;

		LongSet chunksForRadius = new LongOpenHashSet();

		LongSet sentCopy = new LongOpenHashSet(this.loadedChunks);

		LongList chunksToLoad = new LongArrayList();

		for (int x = -radius; x <= radius; ++x) {
			for (int z = -radius; z <= radius; ++z) {
				// Chunk radius is circular, so we need to remove the corners.
				if ((x * x) + (z * z) > radiusSqr) {
					continue;
				}

				var cx = chunkX + x;
				var cz = chunkZ + z;

				var key = ChunkUtils.key(cx, cz);

				chunksForRadius.add(key);
				if (this.loadedChunks.add(key)) {
					chunksToLoad.add(key);
				}
			}
		}

		var loadedChunksChanged = this.loadedChunks.retainAll(chunksForRadius);
		if (loadedChunksChanged || !chunksToLoad.isEmpty()) {
			var packet = new NetworkChunkPublisherUpdatePacket();
			packet.setPosition(this.player.getPosition().toInt());
			packet.setRadius(this.radius);
			this.player.sendPacket(packet);
		}

		// Order chunks for smoother loading
		chunksToLoad.sort(this.chunkComparator);

		var chunkManager = this.player.getWorld().getChunkManager();
		for (var key : chunksToLoad.toLongArray()) {
			var cx = ChunkUtils.fromKeyX(key);
			var cz = ChunkUtils.fromKeyZ(key);

			if (this.sendQueue.putIfAbsent(key, null) == null) {
				chunkManager.generateChunk(cx, cz).thenApply(chunk -> {
					chunk.addLoader(this.player);
					return chunk;
				}).thenApplyAsync(Chunk::createPacket).whenComplete((packet, throwable) -> {
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

		sentCopy.removeAll(chunksForRadius);
		// Remove player from chunk loaders
		sentCopy.forEach(this.removeChunkLoader);
	}

	public long getChunksSent() {
		return this.chunksSentCounter.get();
	}

	public int getChunkRadius() {
		return this.radius >> 4;
	}

	public void setChunkRadius(int chunkRadius) {
		chunkRadius = Math.min(chunkRadius, 32);
		this.setRadius(chunkRadius << 4);
	}

	public void close() {
		this.loadedChunks.forEach(this.removeChunkLoader);

		this.loadedChunks.clear();
	}

	private record AroundPlayerChunkComparator(Player player) implements LongComparator {

		@Override
		public int compare(long chunkHash1, long chunkHash2) {
			int spawnX = this.player.getLocation().getFloorX() >> 4;
			int spawnZ = this.player.getLocation().getFloorZ() >> 4;

			int x1 = ChunkUtils.fromKeyX(chunkHash1);
			int z1 = ChunkUtils.fromKeyZ(chunkHash1);

			int x2 = ChunkUtils.fromKeyX(chunkHash2);
			int z2 = ChunkUtils.fromKeyZ(chunkHash2);

			return Integer.compare(this.distance(spawnX, spawnZ, x1, z1), this.distance(spawnX, spawnZ, x2, z2));
		}

		private int distance(int centerX, int centerZ, int x, int z) {
			var dx = centerX - x;
			var dz = centerZ - z;
			return dx * dx + dz * dz;
		}
	}
}
