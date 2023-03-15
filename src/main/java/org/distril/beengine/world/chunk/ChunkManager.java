package org.distril.beengine.world.chunk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.util.AsyncArrayValue;
import org.distril.beengine.util.ChunkUtils;
import org.distril.beengine.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Log4j2
@RequiredArgsConstructor
public class ChunkManager {

	private final Map<Long, Chunk> chunks = new ConcurrentHashMap<>();

	private final World world;

	public Chunk getLoadedChunk(int x, int z) {
		return this.getLoadedChunk(ChunkUtils.key(x, z));
	}

	public Chunk getLoadedChunk(long key) {
		return this.chunks.get(key);
	}

	public Chunk getChunk(int x, int z) {
		return this.getChunk(ChunkUtils.key(x, z));
	}

	public Chunk getChunk(long key) {
		var chunk = this.getLoadedChunk(key);
		if (chunk == null) {
			chunk = this.generateChunk(key).join();
		}

		return chunk;
	}

	public CompletableFuture<Chunk> generateChunk(int x, int z) {
		return this.generateChunk(ChunkUtils.key(x, z));
	}

	public CompletableFuture<Chunk> generateChunk(long key) {
		return CompletableFuture.supplyAsync(() -> {
			var loadedChunk = this.getLoadedChunk(key);
			if (loadedChunk != null) {
				return loadedChunk;
			}

			var chunk = this.chunks.computeIfAbsent(key, chunkKey -> new Chunk(ChunkUtils.fromKeyX(chunkKey),
					ChunkUtils.fromKeyZ(chunkKey)));

			world.getGenerator().generate(ThreadLocalRandom.current(), chunk);
			return chunk;
		});
	}

	public synchronized void tick() {
		if (this.chunks.isEmpty()) {
			return;
		}

		AsyncArrayValue<Boolean> futureArray = new AsyncArrayValue<>();
		this.chunks.forEach((key, chunk) -> futureArray.add(chunk.tick().whenComplete((close, throwable) -> {
			if (throwable != null) {
				log.error("Error when ticking chunk {}", chunk, throwable);
				return;
			}

			if (close) {
				this.unloadChunk(key, true, false);
			}
		})));

		futureArray.execute().join();
	}

	public void unloadChunk(long key, boolean save, boolean force) {
		var chunk = this.getLoadedChunk(key);
		if (chunk == null) {
			return;
		}

		if (!force && !chunk.getLoaders().isEmpty()) {
			return;
		}

		if (save) {
			// this.saveChunk(chunk);
		}

		chunk.close();

		this.chunks.remove(key);
	}

	public Map<Long, Chunk> getChunks() {
		return new HashMap<>(chunks);
	}
}
