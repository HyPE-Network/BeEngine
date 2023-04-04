package org.distril.beengine.entity

import com.nukkitx.math.vector.Vector3f
import com.nukkitx.protocol.bedrock.BedrockPacket
import com.nukkitx.protocol.bedrock.data.entity.EntityData
import com.nukkitx.protocol.bedrock.data.entity.EntityDataMap
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag
import com.nukkitx.protocol.bedrock.packet.AddEntityPacket
import com.nukkitx.protocol.bedrock.packet.RemoveEntityPacket
import com.nukkitx.protocol.bedrock.packet.SetEntityDataPacket
import org.distril.beengine.entity.data.EntityMetadata
import org.distril.beengine.player.Player
import org.distril.beengine.server.Server
import org.distril.beengine.world.World
import org.distril.beengine.world.chunk.Chunk
import org.distril.beengine.world.util.Location
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

abstract class Entity(val type: EntityType) : EntityMetadata.Listener {

	val viewers: MutableSet<Player> = Collections.newSetFromMap(ConcurrentHashMap())

	val id = NEXT_ID.incrementAndGet()

	val metadata by lazy { EntityMetadata(this) }

	var pitch = 0f
	var yaw = 0f

	lateinit var location: Location
	var maxHealth = 20f
	open var health = 20f
		set(value) {
			if (field == value) return

			if (field < 1) {
				if (this.isAlive) this.kill()
			} else if (value <= this.maxHealth || value < field) {
				field = value
			} else {
				field = this.maxHealth
			}

			this.metadata.setInt(EntityData.HEALTH, field.toInt())
		}

	var isSpawned = false

	open fun onUpdate(currentTick: Long) = this.metadata.update()

	abstract val height: Float

	abstract val width: Float

	open val eyeHeight = 0f

	open fun init(location: Location): Boolean {
		if (this.isSpawned) return false

		this.location = location
		this.metadata.apply {
			this.setFlag(EntityFlag.HAS_COLLISION, true)
			this.setShort(EntityData.AIR_SUPPLY, 400)
			this.setShort(EntityData.MAX_AIR_SUPPLY, 400)
			this.setLong(EntityData.LEASH_HOLDER_EID, -1L)
			this.setFloat(EntityData.SCALE, 1f)
			this.setFloat(EntityData.BOUNDING_BOX_HEIGHT, height)
			this.setFloat(EntityData.BOUNDING_BOX_WIDTH, width)
			this.setInt(EntityData.HEALTH, health.toInt())
		}

		this.world.addEntity(this, true)
		return true
	}

	override fun onDataChange(dataMap: EntityDataMap) = this.sendDataToViewers(dataMap)

	private fun sendDataToViewers(dataMap: EntityDataMap) {
		val packet = SetEntityDataPacket()
		packet.runtimeEntityId = this.id
		packet.metadata.putAll(dataMap)

		Server.broadcastPacket(this.viewers, packet)
	}

	fun sendData(player: Player) {
		val packet = SetEntityDataPacket()
		packet.runtimeEntityId = this.id
		packet.metadata.putAll(this.metadata.data)

		player.sendPacket(packet)
	}

	fun spawnFor(player: Player) {
		if (this.viewers.add(player)) player.sendPacket(this.createSpawnPacket(player))
	}

	protected open fun createSpawnPacket(player: Player): BedrockPacket {
		val packet = AddEntityPacket()
		packet.uniqueEntityId = this.id
		packet.runtimeEntityId = packet.uniqueEntityId
		packet.identifier = this.type.identifier
		packet.position = this.position
		packet.motion = Vector3f.ZERO
		packet.rotation = Vector3f.from(this.pitch, this.yaw, this.yaw)

		return packet
	}

	fun despawnFor(player: Player) {
		if (this.viewers.remove(player)) {
			val packet = RemoveEntityPacket()
			packet.uniqueEntityId = this.id

			player.sendPacket(packet)
		}
	}

	fun despawnForAll() = this.viewers.forEach { this.despawnFor(it) }

	val world: World
		get() = this.location.world

	val chunk: Chunk
		get() = this.location.chunk

	open var position: Vector3f
		get() = this.location.position
		set(value) {
			this.location = Location(this.world, value)
		}

	fun setRotation(pitch: Float, yaw: Float) {
		this.pitch = pitch
		this.yaw = yaw
	}

	val isAlive: Boolean
		get() = this.health > 0

	open fun kill() {
		this.health = 0f
	}

	fun close() {
		if (this.isSpawned) {
			this.isSpawned = false

			this.despawnForAll()

			this.world.removeEntity(this, true)
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Entity) return false

		return this.id == other.id
	}

	override fun hashCode() = (this.id xor (this.id ushr 32)).toInt()

	companion object {

		private val NEXT_ID = AtomicLong(0)
	}
}
