package org.distril.beengine.world.chunk.bitarray

import org.distril.beengine.util.Utils.requireInRange

class Pow2BitArray(version: Version, size: Int, words: IntArray) : BitArray(version, size, words) {

	override fun set(index: Int, value: Int) {
		requireInRange(index, maxValue = this.size, name = "index")
		require(value >= 0 && value <= this.version.maxEntryValue) {
			"Max value: ${this.version.maxEntryValue}. Received value $value"
		}

		val bitIndex = index * this.version.bits
		val arrayIndex = bitIndex shr 5
		val offset = bitIndex and 31
		this.words[arrayIndex] = this.words[arrayIndex] and
				(this.version.maxEntryValue shl offset).inv() or
				(value and this.version.maxEntryValue shl offset)
	}

	override fun get(index: Int): Int {
		requireInRange(index, maxValue = this.size, name = "index")

		val bitIndex = index * this.version.bits
		val arrayIndex = bitIndex shr 5
		val wordOffset = bitIndex and 31
		return this.words[arrayIndex] ushr wordOffset and this.version.maxEntryValue
	}
}
