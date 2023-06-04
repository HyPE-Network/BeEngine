package org.distril.beengine.world.chunk.bitarray

import com.google.common.base.Preconditions

class Pow2BitArray(version: Version, size: Int, words: IntArray) : BitArray(version, size, words) {

    override fun set(index: Int, value: Int) {
        Preconditions.checkElementIndex(index, this.size)
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
        Preconditions.checkElementIndex(index, this.size)

        val bitIndex = index * this.version.bits
        val arrayIndex = bitIndex shr 5
        val wordOffset = bitIndex and 31
        return this.words[arrayIndex] ushr wordOffset and this.version.maxEntryValue
    }
}
