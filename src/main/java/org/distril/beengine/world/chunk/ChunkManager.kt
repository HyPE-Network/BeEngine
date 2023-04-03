package org.distril.beengine.world.chunk

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.distril.beengine.util.ChunkUtils
import org.distril.beengine.world.World
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

class ChunkManager(private val world: World) {

	val chunks: MutableMap<Long, Chunk> = ConcurrentHashMap()
		get() = HashMap(field)

	fun getLoadedChunk(x: Int, z: Int) = this.getLoadedChunk(ChunkUtils.encode(x, z))

	fun getLoadedChunk(key: Long) = this.chunks[key]

	fun getChunk(x: Int, z: Int) = this.getChunk(ChunkUtils.encode(x, z))

	fun getChunk(key: Long): Chunk {
		var chunk = this.getLoadedChunk(key)
		if (chunk == null) {
			chunk = this.generateChunk(key)
		}

		return chunk
	}

	fun generateChunk(x: Int, z: Int) = this.generateChunk(ChunkUtils.encode(x, z))

	fun generateChunk(key: Long): Chunk {
		val loadedChunk = this.getLoadedChunk(key)
		if (loadedChunk != null) return loadedChunk

		val chunk = this.chunks.computeIfAbsent(key) {
			Chunk(
				ChunkUtils.decodeX(it),
				ChunkUtils.decodeZ(it)
			)
		}

		this.world.generator.generate(ThreadLocalRandom.current(), chunk)
		return chunk
	}

	suspend fun tick() {
		if (this.chunks.isNotEmpty()) {
			val tickedChunks = flow {
				chunks.forEach { (key, chunk) ->
					run {
						val canBeUnload = chunk.tick()

						if (canBeUnload) unloadChunk(key)

						emit(Unit)
					}
				}
			}

			tickedChunks.collect()
		}
	}

	fun unloadChunk(key: Long, save: Boolean = true, force: Boolean = false) {
		val chunk = this.getLoadedChunk(key) ?: return
		if (!force && chunk.loaders.isNotEmpty()) return

		if (save) {
			// this.saveChunk(chunk);
		}

		chunk.close()
		this.chunks.remove(key)
	}
}
