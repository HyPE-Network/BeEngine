package org.distril.beengine.world.chunk;

import lombok.RequiredArgsConstructor;
import org.distril.beengine.util.ChunkUtils;
import org.distril.beengine.world.World;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

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
			var chunk = this.chunks.computeIfAbsent(key, chunkKey -> new Chunk(ChunkUtils.fromKeyX(chunkKey),
					ChunkUtils.fromKeyZ(chunkKey)));

			world.getGenerator().generate(ThreadLocalRandom.current(), chunk);
			return chunk;
		});
	}

	public void unloadChunk(long key, boolean save) {
		var chunk = this.getLoadedChunk(key);
		if (chunk == null) {
			return;
		}

		if (!chunk.getLoaders().isEmpty()) {
			return;
		}

		if (save) {
			// this.saveChunk(chunk);
		}

		chunk.close();
	}

	public synchronized void gc() {

	}

	public Map<Long, Chunk> getChunks() {
		return Collections.unmodifiableMap(chunks);
	}
}
