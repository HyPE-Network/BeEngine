package org.distril.beengine.util

import org.distril.beengine.util.Utils.requireInRange

object ChunkUtils {

	fun checkBounds(x: Int, y: Int, z: Int) {
		requireInRange(x, maxValue = 16, name = "X")
		requireInRange(y, maxValue = 256, name = "Y")
		requireInRange(z, maxValue = 16, name = "Z")
	}

	fun decodeX(chunkKey: Long) = (chunkKey shr 32).toInt()

	fun decodeZ(chunkKey: Long) = chunkKey.toInt()

	fun encode(chunkX: Int, chunkZ: Int) = chunkX.toLong() shl 32 or (chunkZ.toLong() and 0xffffffffL)
}
