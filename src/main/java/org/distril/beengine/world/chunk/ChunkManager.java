package org.distril.beengine.world.chunk;

import lombok.RequiredArgsConstructor;
import org.distril.beengine.util.ChunkUtil;
import org.distril.beengine.world.World;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class ChunkManager {

	private final Map<Long, Chunk> chunks = new HashMap<>();

	private final World world;

	public Chunk getChunk(long key) {
		return this.getChunk(ChunkUtil.fromKeyX(key), ChunkUtil.fromKeyZ(key));
	}

	public Chunk getChunk(int x, int z) {
		var chunk = this.chunks.get(ChunkUtil.key(x, z));
		if (chunk == null) {
			chunk = this.generateChunk(x, z).join();
		}

		return chunk;
	}

	public CompletableFuture<Chunk> generateChunk(int chunkX, int chunkZ) {
		var chunkKey = ChunkUtil.key(chunkX, chunkZ);
		var chunk = this.chunks.computeIfAbsent(chunkKey, key -> new Chunk(chunkX, chunkZ));

		return CompletableFuture.supplyAsync(() -> {
			world.getGenerator().generate(ThreadLocalRandom.current(), chunk);
			return chunk;
		});
	}

	public Map<Long, Chunk> getChunks() {
		return Collections.unmodifiableMap(chunks);
	}
}
