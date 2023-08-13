package org.distril.beengine.world.chunk.manager

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.distril.beengine.util.ChunkUtils
import org.distril.beengine.world.World
import org.distril.beengine.world.chunk.Chunk
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

class ChunkManager(private val world: World) {

	val chunks: MutableMap<Long, Chunk> = ConcurrentHashMap()

	val random: Random = ThreadLocalRandom.current()

	fun getLoadedChunk(x: Int, z: Int) = this.getLoadedChunk(ChunkUtils.encode(x, z))

	fun getLoadedChunk(key: Long) = this.chunks[key]

	fun getChunk(x: Int, z: Int) = this.getChunk(ChunkUtils.encode(x, z))

	fun getChunk(key: Long) = this.getLoadedChunk(key) ?: this.generateChunk(key)

	fun generateChunk(x: Int, z: Int) = this.generateChunk(ChunkUtils.encode(x, z))

	fun generateChunk(key: Long): Chunk {
		val loadedChunk = this.getLoadedChunk(key)
		if (loadedChunk != null) return loadedChunk

		val chunk: Chunk
		synchronized(this.chunks) {
			chunk = this.chunks.computeIfAbsent(key) {
				Chunk(ChunkUtils.decodeX(it), ChunkUtils.decodeZ(it))
			}
		}

		this.world.generator.generate(this.random, chunk)
		return chunk
	}

	suspend fun tick() {
		if (chunks.isNotEmpty()) {
			coroutineScope {
				chunks.map { (key, chunk) ->
					async {
						val canBeUnload = chunk.tick()
						if (canBeUnload) unloadChunk(key)
					}
				}.awaitAll()
			}
		}
	}

	fun unloadChunk(key: Long, save: Boolean = true, force: Boolean = false) {
		val chunk = this.getLoadedChunk(key) ?: return
		if (!force && chunk.loaders.isNotEmpty()) return

		// todo: save chunk

		chunk.close()
		this.chunks.remove(key)
	}
}
