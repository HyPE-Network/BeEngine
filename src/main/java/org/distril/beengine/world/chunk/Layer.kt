package org.distril.beengine.world.chunk

import com.nukkitx.network.VarInts
import io.netty.buffer.ByteBuf
import org.distril.beengine.material.Material
import org.distril.beengine.material.block.Block
import org.distril.beengine.material.block.BlockState
import org.distril.beengine.world.chunk.bitarray.BitArray
import java.util.*

class Layer(version: BitArray.Version = BitArray.Version.V2) {

	val palette = mutableListOf<BlockState>()
	var bitArray: BitArray
		private set

	init {
		this.bitArray = version.createPalette(SIZE)
		this.palette.add(AIR_STATE)
	}

	private fun getPaletteHeader(version: BitArray.Version): Int {
		return version.bits.toInt() shl 1 or 1
	}

	operator fun get(x: Int, y: Int, z: Int): BlockState {
		val index = x shl 8 or (z shl 4) or y
		synchronized(this.palette) { return this.palette[bitArray[index]] }
	}

	operator fun set(x: Int, y: Int, z: Int, state: BlockState) {
		try {
			val index = x shl 8 or (z shl 4) or y
			val idx = this.idFor(state)
			this.bitArray[index] = idx
		} catch (exception: IllegalArgumentException) {
			throw IllegalArgumentException("Unable to set block state: $state, palette: $palette", exception)
		}
	}

	fun writeToNetwork(buffer: ByteBuf) {
		buffer.writeByte(this.getPaletteHeader(this.bitArray.version))
		this.bitArray.words.forEach { buffer.writeIntLE(it) }

		synchronized(this.palette) {
			VarInts.writeInt(buffer, this.palette.size)
			this.palette.forEach { VarInts.writeInt(buffer, it.runtimeId) }
		}
	}

	private fun idFor(state: BlockState): Int {
		var index = this.palette.indexOf(state)
		if (index == -1) {
			index = this.palette.size
			val version = this.bitArray.version
			if (index > version.maxEntryValue) {
				val next = version.next
				if (next != null) {
					this.onResize(next)
				}
			}

			this.palette.add(state)
			return index
		}

		return index
	}

	private fun onResize(version: BitArray.Version) {
		val newBitArray = version.createPalette(SIZE)
		for (index in 0 until SIZE) newBitArray[index] = this.bitArray[index]
		this.bitArray = newBitArray
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Layer) return false

		return this.palette != other.palette && this.bitArray != other.bitArray
	}

	override fun hashCode() = Objects.hash(this.palette, this.bitArray)

	companion object {

		private val AIR_STATE = Material.AIR.getBlock<Block>().state

		private const val SIZE = 4096

		fun getVersionFromHeader(header: Byte) = BitArray.Version[header.toInt() shr 1, true]
	}
}
