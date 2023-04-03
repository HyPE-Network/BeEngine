package org.distril.beengine.world.chunk.bitarray

import java.util.*
import kotlin.math.ceil

abstract class BitArray(val version: Version, val size: Int, val words: IntArray) {

	init {
		val expectedWordsLength = ceil((this.size.toFloat() / this.version.entriesPerWord).toDouble()).toInt()

		if (this.words.size != expectedWordsLength) {
			throw IllegalArgumentException(
				"Invalid length given for storage, got: ${this.words.size} but expected: $expectedWordsLength"
			)
		}
	}

	abstract operator fun set(index: Int, value: Int)

	abstract operator fun get(index: Int): Int

	enum class Version(bits: Int, entriesPerWord: Int, val next: Version? = null) {

		V16(16, 2),
		V8(8, 4, V16),
		V6(6, 5, V8), // 2 bit padding
		V5(5, 6, V6), // 2 bit padding
		V4(4, 8, V5),
		V3(3, 10, V4), // 2 bit padding
		V2(2, 16, V3),
		V1(1, 32, V2);

		val bits: Byte
		val entriesPerWord: Byte
		val maxEntryValue: Int

		init {
			this.bits = bits.toByte()
			this.entriesPerWord = entriesPerWord.toByte()
			this.maxEntryValue = (1 shl this.bits.toInt()) - 1
		}

		fun getWordsForSize(size: Int) = size / this.entriesPerWord + if (size % this.entriesPerWord == 0) 0 else 1

		fun createPalette(size: Int, words: IntArray = IntArray(this.getWordsForSize(size))) =
			if (this == V3 || this == V5 || this == V6) {
				PaddedBitArray(this, size, words)
			} else {
				Pow2BitArray(this, size, words)
			}

		companion object {

			operator fun get(version: Int, read: Boolean): Version {
				return Arrays.stream(Version.values())
					.filter { !read && it.entriesPerWord <= version || read && it.bits.toInt() == version }
					.findFirst()
					.orElseThrow { IllegalArgumentException("Invalid palette version: $version") }
			}
		}
	}
}
