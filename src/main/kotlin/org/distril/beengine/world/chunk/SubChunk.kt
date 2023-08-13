package org.distril.beengine.world.chunk

import io.netty.buffer.ByteBuf
import org.distril.beengine.material.block.BlockState
import java.util.*

class SubChunk(val index: Int) {

	val layers = arrayOf(Layer(), Layer())

	fun setBlockState(x: Int, y: Int, z: Int, state: BlockState, layer: Int = 0) {
		synchronized(this.layers) {
			this.layers[layer][x and 0xf, y and 0xf, z and 0xf] = state
		}
	}

	fun getBlockState(x: Int, y: Int, z: Int, layer: Int = 0) = this.layers[layer][x and 0xf, y and 0xf, z and 0xf]

	fun writeToNetwork(buffer: ByteBuf) {
		buffer.writeByte(VERSION)
		buffer.writeByte(this.layers.size)
		buffer.writeByte(this.index)

		synchronized(this.layers) {
			this.layers.forEach { it.writeToNetwork(buffer) }
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is SubChunk) return false

		return this.index == other.index && this.layers.contentEquals(other.layers)
	}

	override fun hashCode() = Objects.hash(this.index, this.layers.contentHashCode())

	companion object {

		const val VERSION = 9

		const val COUNT = 16
	}
}
