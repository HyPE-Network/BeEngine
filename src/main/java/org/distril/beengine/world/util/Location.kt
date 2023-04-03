package org.distril.beengine.world.util

import com.nukkitx.math.vector.Vector3f
import org.distril.beengine.world.World
import org.distril.beengine.world.chunk.Chunk
import java.util.*

class Location(val world: World, val position: Vector3f = Vector3f.ZERO) {

	val x: Float
		get() = this.position.x
	val y: Float
		get() = this.position.y
	val z: Float
		get() = this.position.z

	val floorX: Int
		get() = this.position.floorX
	val floorY: Int
		get() = this.position.floorY
	val floorZ: Int
		get() = this.position.floorZ

	val chunkX: Int
		get() = this.floorX shr 4
	val chunkZ: Int
		get() = this.floorZ shr 4

	val chunk: Chunk
		get() = this.world.getChunk(this.chunkX, this.chunkZ)

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Location) return false

		return this.world == other.world && this.position == other.position
	}

	override fun hashCode() = Objects.hash(this.world, this.position)
}
