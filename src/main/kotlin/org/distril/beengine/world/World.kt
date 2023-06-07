package org.distril.beengine.world

import com.nukkitx.math.vector.Vector3i
import com.nukkitx.protocol.bedrock.packet.UpdateBlockPacket
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.distril.beengine.Tickable
import org.distril.beengine.entity.Entity
import org.distril.beengine.material.Material
import org.distril.beengine.material.block.Block
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player
import org.distril.beengine.server.Server
import org.distril.beengine.util.Direction
import org.distril.beengine.world.chunk.manager.ChunkManager
import org.distril.beengine.world.chunk.processor.PlayerChunkProcessor
import org.distril.beengine.world.chunk.processor.PlayerChunkRequest
import org.distril.beengine.world.generator.Generator
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class World(val worldName: String, val dimension: Dimension, val generator: Generator) : Tickable(
	"$worldName-World"
) {

	val path = Path.of("worlds", worldName)
	val chunkManager = ChunkManager(this)
	val playerChunkProcessor = PlayerChunkProcessor(this.worldName)
	val entities: MutableMap<Long, Entity> = ConcurrentHashMap()

	init {
		this.start()
	}

	override suspend fun onUpdate(currentTick: Long): Unit = coroutineScope {
		val tickedItems = mutableListOf<Deferred<Any>>()
		tickedItems.addAll(chunkManager.tick())

		entities.values.forEach {
			tickedItems.add(async { it.onUpdate(currentTick) })
		}

		awaitAll(*tickedItems.toTypedArray())
	}

	fun addPlayerRequest(request: PlayerChunkRequest) {
		this.playerChunkProcessor.addRequest(request)
	}

	fun addEntity(entity: Entity, addInChunk: Boolean = true) {
		this.entities[entity.id] = entity

		if (addInChunk) {
			val location = entity.location
			location.chunk.addEntity(entity)
		}
	}

	fun removeEntity(entity: Entity, removeInChunk: Boolean = true) {
		this.entities.remove(entity.id)

		if (removeInChunk) {
			val location = entity.location
			location.chunk.removeEntity(entity)
		}
	}

	fun getLoadedBlock(position: Vector3i, layer: Int = 0) =
		this.getLoadedBlock(position.x, position.y, position.z, layer)

	fun getLoadedBlock(x: Int, y: Int, z: Int, layer: Int = 0): Block? {
		if (y >= this.dimension.maxY || y < this.dimension.minY) {
			return Material.AIR.getBlock<Block>().apply {
				world = this@World
				position = Vector3i.from(x, y, z)
			}
		}

		val chunkX = x shr 4
		val chunkZ = z shr 4

		val chunk = this.chunkManager.getLoadedChunk(chunkX, chunkZ) ?: return null

		return chunk.getBlock(x and 0xf, y, z and 0xf, layer).apply {
			world = this@World
			position = Vector3i.from(x, y, z)
		}
	}

	fun getBlock(position: Vector3i, layer: Int = 0) = this.getBlock(position.x, position.y, position.z, layer)

	fun getBlock(x: Int, y: Int, z: Int, layer: Int = 0): Block {
		val loadedBlock = this.getLoadedBlock(x, y, z, layer)
		if (loadedBlock != null) return loadedBlock

		val chunkX = x shr 4
		val chunkZ = z shr 4

		val chunk = this.chunkManager.getChunk(chunkX, chunkZ)

		return chunk.getBlock(x and 0xf, y, z and 0xf, layer).apply {
			world = this@World
			position = Vector3i.from(x, y, z)
		}
	}

	fun setBlock(position: Vector3i, block: Block, layer: Int = 0) =
		this.setBlock(position.x, position.y, position.z, block, layer)

	fun setBlock(x: Int, y: Int, z: Int, block: Block, layer: Int = 0, send: Boolean = true) {
		if (y >= this.dimension.maxY || y < this.dimension.minY) return

		val chunk = this.chunkManager.getChunk(x shr 4, z shr 4)
		chunk.setBlock(x and 0xF, y, z and 0xF, block, layer)

		if (send) this.sendBlocks(chunk.players, listOf(block))
	}

	fun sendBlocks(
		targets: Collection<Player>,
		blocks: Collection<Block>,
		flags: Set<UpdateBlockPacket.Flag> = UpdateBlockPacket.FLAG_ALL_PRIORITY
	) {
		val packets = mutableListOf<UpdateBlockPacket>()

		val packet = UpdateBlockPacket()
		packet.flags.addAll(flags)
		blocks.forEach {
			packet.blockPosition = it.position
			packet.runtimeId = it.state.runtimeId
			packet.dataLayer = 0
			packets.add(packet)

			packet.dataLayer = 1
			packets.add(packet)
		}

		Server.broadcastPackets(targets, packets)
	}

	fun getLoadedChunkEntities(chunkX: Int, chunkZ: Int): Collection<Entity> {
		return this.chunkManager.getLoadedChunk(chunkX, chunkZ)?.entities ?: return setOf()
	}

	fun useItemOn(blockPosition: Vector3i, usedItem: Item, clickedBlockFace: Direction, player: Player): Item? {
		val clickedBlock = this.getBlock(blockPosition)
		if (clickedBlock.material === Material.AIR) return null

		val replaceBlock = clickedBlock.getSide(clickedBlockFace)

		val replacePos = replaceBlock.position!!
		if (replacePos.y < this.dimension.maxY && replacePos.y >= this.dimension.minY) {
			usedItem.toBlock<Block>()?.apply {
				world = this@World
				position = replacePos
			}?.run { setBlock(replacePos, block = this) }

			return usedItem
		}

		return null
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is World) return false

		return this.path == other.path
	}

	override fun hashCode() = Objects.hash(this.path)
}
