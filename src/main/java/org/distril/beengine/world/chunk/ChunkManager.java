package org.distril.beengine.world.chunk;

import lombok.RequiredArgsConstructor;
import org.distril.beengine.util.ChunkUtil;
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
		return this.getLoadedChunk(ChunkUtil.key(x, z));
	}

	public Chunk getLoadedChunk(long key) {
		return this.chunks.get(key);
	}

	public Chunk getChunk(int x, int z) {
		return this.getChunk(ChunkUtil.key(x, z));
	}

	public Chunk getChunk(long key) {
		var chunk = this.getLoadedChunk(key);
		if (chunk == null) {
			chunk = this.generateChunk(key).join();
		}

		return chunk;
	}

	public CompletableFuture<Chunk> generateChunk(int x, int z) {
		return this.generateChunk(ChunkUtil.key(x, z));
	}

	public CompletableFuture<Chunk> generateChunk(long key) {
		return CompletableFuture.supplyAsync(() -> {
			var chunk = this.chunks.computeIfAbsent(key, chunkKey -> new Chunk(ChunkUtil.fromKeyX(chunkKey),
					ChunkUtil.fromKeyZ(chunkKey)));

			world.getGenerator().generate(ThreadLocalRandom.current(), chunk);
			return chunk;
		});
	}

	public Map<Long, Chunk> getChunks() {
		return Collections.unmodifiableMap(chunks);
	}
}
