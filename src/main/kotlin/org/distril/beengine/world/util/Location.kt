package org.distril.beengine.world.util

import com.nukkitx.math.vector.Vector3f
import org.distril.beengine.world.World

data class Location(val world: World, val position: Vector3f = Vector3f.ZERO) {

	val x = this.position.x
	val y = this.position.y
	val z = this.position.z

	val floorX = this.position.floorX
	val floorY = this.position.floorY
	val floorZ = this.position.floorZ

	val chunkX = this.floorX shr 4
	val chunkZ = this.floorZ shr 4

	val chunk get() = this.world.chunkManager.getChunk(this.chunkX, this.chunkZ)
}
