package org.distril.beengine.player.manager;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;
import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;
import it.unimi.dsi.fastutil.longs.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.entity.Entity;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.ChunkUtil;
import org.distril.beengine.world.chunk.Chunk;

import java.util.concurrent.atomic.AtomicLong;

@Getter
@Log4j2
public class PlayerChunkManager {

	private final Player player;

	private final AroundPlayerChunkComparator comparator;

	private final LongSet loadedChunks = new LongOpenHashSet();
	private final Long2ObjectMap<LevelChunkPacket> sendQueue = new Long2ObjectOpenHashMap<>();
	private final AtomicLong chunksSentCounter = new AtomicLong();

	private volatile  int radius;

	public PlayerChunkManager(Player player) {
		this.player = player;
		this.comparator = new AroundPlayerChunkComparator(player);
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
		int chunksPerTick = 16; // this.player.getServer().getConfig("chunk-sending.per-tick", 4);

		LongList list = new LongArrayList(this.sendQueue.keySet());

		// Order chunks around player.
		list.unstableSort(this.comparator);

		for (long key : list.toLongArray()) {
			if (chunksPerTick < 0) {
				break;
			}

			LevelChunkPacket packet = this.sendQueue.get(key);
			if (packet == null) {
				// Next packet is not available.
				break;
			}

			this.sendQueue.remove(key);
			this.player.sendPacket(packet);

			var chunk = this.player.getWorld().getChunkManager().getLoadedChunk(key);
			if (chunk == null) {
				log.warn("Attempted to send unloaded chunk ({}, {}) to {}", ChunkUtil.fromKeyX(key), ChunkUtil.fromKeyZ(key)
						, this.player.getName());
				return;
			}

			// Spawn entities
			for (Entity entity : chunk.getEntities().values()) {
				if (entity != this.player && !entity.isSpawned()) {
					entity.spawnTo(this.player);
				}
			}

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
		int radius = this.getChunkRadius();
		int radiusSqr = radius * radius;

		LongSet chunksForRadius = new LongOpenHashSet();

		LongSet sentCopy = new LongOpenHashSet(this.loadedChunks);

		LongList chunksToLoad = new LongArrayList();

		for (int x = -radius; x <= radius; ++x) {
			for (int z = -radius; z <= radius; ++z) {
				// Chunk radius is circular, so we need to remove the corners.
				if ((x * x) + (z * z) > radiusSqr) {
					continue;
				}

				int cx = chunkX + x;
				int cz = chunkZ + z;

				final long key = ChunkUtil.key(cx, cz);

				chunksForRadius.add(key);
				if (this.loadedChunks.add(key)) {
					chunksToLoad.add(key);
				}
			}
		}

		boolean loadedChunksChanged = this.loadedChunks.retainAll(chunksForRadius);
		if (loadedChunksChanged || !chunksToLoad.isEmpty()) {
			NetworkChunkPublisherUpdatePacket packet = new NetworkChunkPublisherUpdatePacket();
			packet.setPosition(this.player.getPosition().toInt());
			packet.setRadius(this.radius);
			this.player.sendPacket(packet);
		}

		// Order chunks for smoother loading
		chunksToLoad.sort(this.comparator);

		for (final long key : chunksToLoad.toLongArray()) {
			final int cx = ChunkUtil.fromKeyX(key);
			final int cz = ChunkUtil.fromKeyZ(key);

			if (this.sendQueue.putIfAbsent(key, null) == null) {
				this.player.getWorld().getChunkManager().generateChunk(cx, cz).thenApply(Chunk::createPacket)
						.whenComplete((packet, throwable) -> {
							synchronized (PlayerChunkManager.this) {
								if (throwable != null) {
									if (this.sendQueue.remove(key, null)) {
										this.loadedChunks.remove(key);
									}

									log.error("Unable to create chunk packet for " + this.player.getName(), throwable);
								} else if (!this.sendQueue.replace(key, null, packet)) {
									// The chunk was already loaded!?
									if (this.sendQueue.containsKey(key)) {
										log.warn("Chunk ({},{}) already loaded for {}, value {}", cx, cz,
												this.player.getName(), this.sendQueue.get(key));
									}
								}
							}
						});
			}
		}

		sentCopy.removeAll(chunksForRadius);
		// Remove player from chunk loaders
		// sentCopy.forEach(this.removeChunkLoader);
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

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static class AroundPlayerChunkComparator implements LongComparator {

		private final Player player;

		@Override
		public int compare(long o1, long o2) {
			int x1 = ChunkUtil.fromKeyX(o1);
			int z1 = ChunkUtil.fromKeyZ(o1);
			int x2 = ChunkUtil.fromKeyX(o2);
			int z2 = ChunkUtil.fromKeyZ(o2);
			int spawnX = this.player.getPosition().getFloorX() >> 4;
			int spawnZ = this.player.getPosition().getFloorZ() >> 4;

			return Integer.compare(this.distance(spawnX, spawnZ, x1, z1), this.distance(spawnX, spawnZ, x2, z2));
		}

		private int distance(int centerX, int centerZ, int x, int z) {
			int dx = centerX - x;
			int dz = centerZ - z;
			return dx * dx + dz * dz;
		}
	}
}
