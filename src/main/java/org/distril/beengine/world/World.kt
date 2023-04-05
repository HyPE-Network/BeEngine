package org.distril.beengine.world

import com.nukkitx.math.vector.Vector3i
import com.nukkitx.protocol.bedrock.packet.UpdateBlockPacket
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.distril.beengine.Tickable
import org.distril.beengine.entity.Entity
import org.distril.beengine.material.Material
import org.distril.beengine.material.block.Block
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player
import org.distril.beengine.server.Server
import org.distril.beengine.util.Direction
import org.distril.beengine.world.chunk.ChunkManager
import org.distril.beengine.world.generator.Generator
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class World(val worldName: String, val dimension: Dimension, val generator: Generator) : Tickable(
	"$worldName - World"
) {

	val path = Path.of("worlds", worldName)
	val chunkManager = ChunkManager(this)
	val entities: MutableMap<Long, Entity> = ConcurrentHashMap<Long, Entity>()

	init {
		this.start()
	}

	override fun onUpdate(currentTick: Long) {
		runBlocking {
			val list = mutableListOf(
				async {
					val tickedEntities = flow { entities.values.forEach { emit(it.onUpdate(currentTick)) } }

					tickedEntities.collect()
				},
				async { chunkManager.tick() }
			)

			awaitAll(*list.toTypedArray())
		}
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

	fun getLoadedChunk(x: Int, z: Int) = this.chunkManager.getLoadedChunk(x, z)

	fun getLoadedChunk(key: Long) = this.chunkManager.getLoadedChunk(key)

	fun getChunk(position: Vector3i) = this.chunkManager.getChunk(position.x shr 4, position.z shr 4)

	fun getChunk(x: Int, z: Int) = this.chunkManager.getChunk(x, z)

	fun getChunk(key: Long) = this.chunkManager.getChunk(key)

	fun getLoadedBlock(position: Vector3i) = this.getLoadedBlock(position.x, position.y, position.z)

	fun getLoadedBlock(x: Int, y: Int, z: Int, layer: Int = 0): Block? {
		if (y >= MAX_Y || y < MIN_Y) return Material.AIR.getBlock<Block>().apply {
			world = this@World
			position = Vector3i.from(x, y, z)
		}

		val chunkX = x shr 4
		val chunkZ = z shr 4

		val chunk = this.getLoadedChunk(chunkX, chunkZ) ?: return null

		return chunk.getBlock(x and 0xf, y, z and 0xf, layer).apply {
			world = this@World
			position = Vector3i.from(x, y, z)
		}
	}

	fun getBlock(position: Vector3i) = this.getBlock(position.x, position.y, position.z)

	fun getBlock(x: Int, y: Int, z: Int, layer: Int = 0): Block {
		val loadedBlock = this.getLoadedBlock(x, y, z, layer)
		if (loadedBlock != null) return loadedBlock

		val chunkX = x shr 4
		val chunkZ = z shr 4

		val chunk = this.getChunk(chunkX, chunkZ)

		return chunk.getBlock(x and 0xf, y, z and 0xf, layer).apply {
			world = this@World
			position = Vector3i.from(x, y, z)
		}
	}

	fun setBlock(position: Vector3i, layer: Int = 0, block: Block) {
		this.setBlock(position.x, position.y, position.z, layer, block)
	}

	fun setBlock(x: Int, y: Int, z: Int, layer: Int = 0, block: Block) {
		this.setBlock(x, y, z, layer, block, true)
	}

	fun setBlock(x: Int, y: Int, z: Int, layer: Int = 0, block: Block, send: Boolean = true) {
		if (y >= MAX_Y || y < MIN_Y) return

		val chunk = this.getChunk(x shr 4, z shr 4)
		chunk.setBlock(x and 0xF, y, z and 0xF, layer, block)

		if (send) this.sendBlocks(chunk.getPlayers(), listOf(block))
	}

	fun sendBlocks(
		targets: Collection<Player>,
		blocks: Collection<Block>,
		flags: Set<UpdateBlockPacket.Flag> = UpdateBlockPacket.FLAG_ALL_PRIORITY
	) {
		val packets = mutableListOf<UpdateBlockPacket>()
		blocks.forEach {
			val packet = UpdateBlockPacket()
			packet.flags.addAll(flags)
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
		val loadedChunk = this.getLoadedChunk(chunkX, chunkZ) ?: return mutableSetOf()
		return loadedChunk.entities
	}

	fun useItemOn(blockPosition: Vector3i, usedItem: Item, clickedBlockFace: Direction, player: Player): Item? {
		val clickedBlock = this.getBlock(blockPosition)
		if (clickedBlock.material === Material.AIR) return null

		val replaceBlock = clickedBlock.getSide(clickedBlockFace)

		val replacePos = replaceBlock.position!!
		if (replacePos.y >= MAX_Y || replacePos.y < MIN_Y) return null

		usedItem.toBlock<Block>()?.apply {
			world = this@World
			position = replacePos
		}?.run { setBlock(replacePos, block = this) }

		return usedItem
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is World) return false

		return this.path == other.path
	}

	override fun hashCode() = Objects.hash(this.path)

	companion object {

		private val log = LogManager.getLogger(Player::class.java)

		private const val MAX_Y = 256

		private const val MIN_Y = 0
	}
}
