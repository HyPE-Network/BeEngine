package org.distril.beengine.util

import com.google.common.base.Preconditions

object ChunkUtils {

    fun checkBounds(x: Int, y: Int, z: Int) {
        Preconditions.checkElementIndex(x, 16, "X coordinate")
        Preconditions.checkElementIndex(y, 256, "Y coordinate")
        Preconditions.checkElementIndex(z, 16, "Z coordinate")
    }

    fun decodeX(chunkKey: Long) = (chunkKey shr 32).toInt()

    fun decodeZ(chunkKey: Long) = chunkKey.toInt()

    fun encode(chunkX: Int, chunkZ: Int) = chunkX.toLong() shl 32 or (chunkZ.toLong() and 0xffffffffL)
}
