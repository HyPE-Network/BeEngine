package org.distril.beengine.world.chunk.bitarray

import org.distril.beengine.util.Utils.requireInRange

class PaddedBitArray(version: Version, size: Int, words: IntArray) : BitArray(version, size, words) {

	override fun set(index: Int, value: Int) {
		requireInRange(index, maxValue = this.size, name = "index")
		require(value >= 0 && value <= this.version.maxEntryValue) {
			"Max value: ${this.version.maxEntryValue}. Received value $value"
		}

		val arrayIndex = index / this.version.entriesPerWord
		val offset = index % this.version.entriesPerWord * this.version.bits
		this.words[arrayIndex] = this.words[arrayIndex] and
				(this.version.maxEntryValue shl offset).inv() or
				(value and this.version.maxEntryValue shl offset)
	}

	override fun get(index: Int): Int {
		requireInRange(index, maxValue = this.size, name = "index")

		val arrayIndex = index / this.version.entriesPerWord
		val offset = index % this.version.entriesPerWord * this.version.bits
		return this.words[arrayIndex] ushr offset and this.version.maxEntryValue
	}
}
