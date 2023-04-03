package org.distril.beengine.world.chunk.bitarray

import com.google.common.base.Preconditions

class PaddedBitArray(version: Version, size: Int, words: IntArray) : BitArray(version, size, words) {

	override fun set(index: Int, value: Int) {
		Preconditions.checkElementIndex(index, this.size)
		require(value >= 0 && value <= this.version.maxEntryValue) {
			"Max value: ${this.version.maxEntryValue}. Received value $value"
		}

		val arrayIndex = index / this.version.entriesPerWord
		val offset = index % this.version.entriesPerWord * this.version.bits
		this.words[arrayIndex] =
			this.words[arrayIndex] and (this.version.maxEntryValue shl offset).inv() or (value and this.version.maxEntryValue shl offset)
	}

	override fun get(index: Int): Int {
		Preconditions.checkElementIndex(index, this.size)

		val arrayIndex = index / this.version.entriesPerWord
		val offset = index % this.version.entriesPerWord * this.version.bits
		return this.words[arrayIndex] ushr offset and this.version.maxEntryValue
	}
}
