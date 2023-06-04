package org.distril.beengine.player.manager

import com.nukkitx.math.vector.Vector3f
import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket
import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket
import it.unimi.dsi.fastutil.longs.LongArrayList
import it.unimi.dsi.fastutil.longs.LongComparator
import it.unimi.dsi.fastutil.longs.LongConsumer
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import org.distril.beengine.player.Player
import org.distril.beengine.util.ChunkUtils
import org.distril.beengine.world.chunk.processor.SendChunk
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class PlayerChunkManager(val player: Player) {

    private val chunkComparator = AroundPlayerChunkComparator(this.player)

    private val removeChunkLoader = LongConsumer {
        this.player.world.chunkManager.getLoadedChunk(it)?.let { chunk ->
            chunk.removeLoader(this.player)
            chunk.entities.forEach { entity -> entity.despawnFor(this.player) }
        }
    }

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

    fun succesfull(chunkHash: Long) {
        this.loadedChunks.add(chunkHash)
        this.chunksSentCounter.incrementAndGet()
    }

    fun queueNewChunks(position: Vector3f = this.player.position) =
        this.queueNewChunks(position.floorX shr 4, position.floorZ shr 4)

    fun queueNewChunks(fromChunkX: Int, fromChunkZ: Int) {
        synchronized(this) {
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

                if (!this.loadedChunks.contains(key)) chunksToLoad.add(key)
            }

            val loadedChunksChanged = this.loadedChunks.retainAll(chunksForRadius)
            if (loadedChunksChanged || chunksToLoad.isNotEmpty()) {
                val packet = NetworkChunkPublisherUpdatePacket()
                packet.position = this.player.position.toInt()
                packet.radius = this.radius shl 4

                this.player.sendPacket(packet)
            }

            // Order chunks for smoother loading
            chunksToLoad.unstableSort(this.chunkComparator)

            val world = this.player.world
            val chunkManager = world.chunkManager
            chunksToLoad.forEach {
                val chunkX = ChunkUtils.decodeX(it)
                val chunkZ = ChunkUtils.decodeZ(it)

                val chunk = chunkManager.getChunk(chunkX, chunkZ)
                world.addPlayerRequest(SendChunk(this.player, chunk))

                sentCopy.removeAll(chunksForRadius)
                // Remove player from chunk loaders
                sentCopy.forEach(removeChunkLoader)
            }
        }
    }

    val chunksSentCount get() = this.chunksSentCounter.get()

    fun clear() {
        this.loadedChunks.forEach(this.removeChunkLoader)
        this.loadedChunks.clear()
    }

    companion object {

        private const val MAX_RADIUS = 32
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
