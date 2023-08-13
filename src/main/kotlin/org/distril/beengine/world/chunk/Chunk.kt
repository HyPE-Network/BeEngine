package org.distril.beengine.world.chunk

import com.nukkitx.network.VarInts
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket
import io.netty.buffer.Unpooled
import org.distril.beengine.entity.Entity
import org.distril.beengine.material.Material
import org.distril.beengine.material.block.Block
import org.distril.beengine.player.Player
import org.distril.beengine.server.Server
import org.distril.beengine.util.ChunkUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class Chunk(val x: Int, val z: Int) {

	val subChunks = arrayOfNulls<SubChunk>(SubChunk.COUNT)

	val entities: MutableSet<Entity> = Collections.newSetFromMap(ConcurrentHashMap())

	val loaders = mutableSetOf<ChunkLoader>()

	private var expiryTime: Int = 0

	init {
		this.resetExpiryTime()
	}

	fun setBlock(x: Int, y: Int, z: Int, block: Block, layer: Int = 0) {
		ChunkUtils.checkBounds(x, y, z)

		var subChunk = this.subChunks[y shr 4]
		if (subChunk == null) {
			if (block.material === Material.AIR) return

			subChunk = this.getOrCreateSubChunk(y shr 4)
		}

		subChunk.setBlockState(x, y, z, block.state, layer)
	}

	fun getBlock(x: Int, y: Int, z: Int, layer: Int = 0): Block {
		ChunkUtils.checkBounds(x, y, z)

		val subChunk = this.subChunks[y shr 4]
		val state = subChunk?.getBlockState(x, y, z, layer) ?: AIR_STATE
		return Server.blockRegistry.getBlockFromState(state)
	}

	private fun getOrCreateSubChunk(index: Int): SubChunk {
		for (y in index downTo 0) {
			var subChunk = this.subChunks[y]
			if (subChunk == null) {
				subChunk = SubChunk(y)
				this.subChunks[y] = subChunk
			}
		}

		return this.subChunks[index]!!
	}

	fun addEntity(entity: Entity) {
		this.entities.add(entity)
	}

	fun removeEntity(entity: Entity) {
		this.entities.remove(entity)
	}

	fun addLoader(loader: ChunkLoader) {
		if (this.loaders.add(loader)) {
			this.resetExpiryTime()
		}
	}

	fun removeLoader(loader: ChunkLoader) {
		this.loaders.remove(loader)
	}

	val playersLoader get() = this.loaders.filterIsInstance<Player>()

	val players get() = this.entities.filterIsInstance<Player>()

	fun tick(): Boolean {
		// todo: tick block updates and block entities
		if (this.expiryTime > 0 && this.canBeClosed) {
			this.expiryTime--
			return this.expiryTime == 0
		}

		return false
	}

	val canBeClosed get() = this.loaders.isEmpty()

	fun createPacket(): LevelChunkPacket {
		val packet = LevelChunkPacket()
		packet.chunkX = x
		packet.chunkZ = z
		packet.subChunksLength = this.subChunks.count { it != null }

		val buffer = Unpooled.buffer()
		try {
			for (subChunk in this.subChunks) {
				if (subChunk == null) break

				subChunk.writeToNetwork(buffer)
			}

			buffer.writeBytes(ByteArray(256)) // todo: biomes
			buffer.writeByte(0) // Border blocks size - Education Edition only
			VarInts.writeUnsignedInt(buffer, 0) // Extra Data length. Replaced by second block layer.

			val data = ByteArray(buffer.readableBytes())
			buffer.readBytes(data)

			packet.data = data
			return packet
		} finally {
			buffer.release()
		}
	}

	fun hash() = ChunkUtils.encode(this.x, this.z)

	fun close() {
		this.subChunks.fill(null)

		this.entities.forEach { if (it !is Player) it.close() }
		this.entities.clear()

		this.loaders.clear()
	}

	private fun resetExpiryTime() {
		this.expiryTime = Server.settings.chunkExpiryTime * 20
	}

	companion object {

		private val AIR_STATE = Material.AIR.getBlock<Block>().state
	}
}
