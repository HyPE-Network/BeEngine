package org.distril.beengine.player.manager

import com.nukkitx.math.vector.Vector3f
import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket
import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket
import it.unimi.dsi.fastutil.longs.LongArrayList
import it.unimi.dsi.fastutil.longs.LongComparator
import it.unimi.dsi.fastutil.longs.LongConsumer
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.distril.beengine.player.Player
import org.distril.beengine.util.ChunkUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class PlayerChunkManager(val player: Player) {

	private val chunkComparator = AroundPlayerChunkComparator(this.player)

	private val removeChunkLoader = LongConsumer { chunkKey ->
		val chunk = this.player.world.getLoadedChunk(chunkKey)
		if (chunk != null) {
			chunk.removeLoader(this.player)
			chunk.entities.forEach { it.despawnFor(this.player) }
		}
	}

	private val sendQueue: MutableMap<Long, LevelChunkPacket?> = HashMap()
	private val chunksSentCounter = AtomicLong()

	val loadedChunks: MutableSet<Long> = Collections.newSetFromMap(ConcurrentHashMap())

	var radius = MAX_RADIUS
		set(value) {
			if (field != value) {
				field = value

				this.player.sendPacket(ChunkRadiusUpdatedPacket().apply {
					this.radius = value
				})

				this.queueNewChunks()
			}
		}

	@Synchronized
	fun sendQueued() {
		// Remove chunks which are out of range
		with(this.sendQueue.entries.iterator()) {
			forEach {
				val key = it.key
				if (!loadedChunks.contains(key)) {
					this.remove()
					val chunk = player.world.getLoadedChunk(key)
					chunk?.removeLoader(player)
				}
			}
		}

		val list = LongArrayList(this.sendQueue.keys)

		// Order chunks around player.
		list.unstableSort(chunkComparator)

		list.forEach { chunkKey ->
			val packet = this.sendQueue.remove(chunkKey) ?: return

			this.player.sendPacket(packet)

			val chunk = this.player.world.chunkManager.getLoadedChunk(chunkKey)
			if (chunk == null) {
				log.warn(
					"Attempted to send unloaded chunk (${ChunkUtils.decodeX(chunkKey)}:" +
							"${ChunkUtils.decodeZ(chunkKey)}) to " + player.name
				)

				return
			}

			// Spawn entities
			chunk.entities.forEach { if (it != this.player && !it.isSpawned) it.spawnFor(player) }

			this.chunksSentCounter.incrementAndGet()
		}
	}

	fun queueNewChunks(position: Vector3f = this.player.position) {
		this.queueNewChunks(position.floorX shr 4, position.floorZ shr 4)
	}

	@Synchronized
	fun queueNewChunks(fromChunkX: Int, fromChunkZ: Int) {
		val radiusSqr = this.radius * this.radius

		val chunksForRadius = mutableSetOf<Long>()
		val sentCopy = LongOpenHashSet(this.loadedChunks)

		val chunksToLoad = LongArrayList()
		for (x in -this.radius..this.radius) for (z in -this.radius..this.radius) {
			if (((x * x) + (z * z)) > radiusSqr) continue

			val chunkX = fromChunkX + x
			val chunkZ = fromChunkZ + z

			val key = ChunkUtils.encode(chunkX, chunkZ)
			chunksForRadius.add(key)

			if (this.loadedChunks.add(key)) chunksToLoad.add(key)
		}

		val loadedChunksChanged = this.loadedChunks.retainAll(chunksForRadius)
		if (loadedChunksChanged || chunksToLoad.isNotEmpty()) {
			val packet = NetworkChunkPublisherUpdatePacket()
			packet.position = this.player.position.toInt()
			packet.radius = this.radius shl 4

			this.player.sendPacket(packet)
		}

		// Order chunks for smoother loading
		chunksToLoad.sort(this.chunkComparator)

		val chunkManager = this.player.world.chunkManager

		val chunks = flow {
			chunksToLoad.forEach {
				val chunkX = ChunkUtils.decodeX(it)
				val chunkZ = ChunkUtils.decodeZ(it)
				if (sendQueue.putIfAbsent(it, null) == null) {
					val chunk = chunkManager.getChunk(chunkX, chunkZ)
					chunk.addLoader(player)

					if (!sendQueue.replace(it, null, chunk.createPacket())) {
						if (sendQueue.containsKey(it)) {
							log.warn(
								"Chunk ($chunkX:$chunkZ) already loaded for ${player.name}, value ${sendQueue[it]}"
							)
						}
					}
				}

				emit(null)
			}
		}

		runBlocking(Dispatchers.IO) { chunks.collect() }

		sentCopy.removeAll(chunksForRadius)
		// Remove player from chunk loaders
		sentCopy.forEach(removeChunkLoader)
	}

	val chunksSentCount: Long
		get() = this.chunksSentCounter.get()

	fun clear() {
		this.loadedChunks.forEach(this.removeChunkLoader)
		this.loadedChunks.clear()
	}

	companion object {

		private const val MAX_RADIUS = 32

		private val log = LogManager.getLogger(PlayerChunkManager::class.java)
	}

	class AroundPlayerChunkComparator(val player: Player) : LongComparator {

		override fun compare(chunkKey1: Long, chunkKey2: Long): Int {
			val spawnX = this.player.location.chunkX
			val spawnZ = this.player.location.chunkZ

			val x1 = ChunkUtils.decodeX(chunkKey1)
			val z1 = ChunkUtils.decodeZ(chunkKey1)

			val x2 = ChunkUtils.decodeX(chunkKey2)
			val z2 = ChunkUtils.decodeZ(chunkKey2)
			return this.distance(spawnX, spawnZ, x1, z1).compareTo(this.distance(spawnX, spawnZ, x2, z2))
		}

		private fun distance(centerX: Int, centerZ: Int, x: Int, z: Int): Int {
			val dx = centerX - x
			val dz = centerZ - z
			return (dx * dx) + (dz * dz)
		}
	}
}
