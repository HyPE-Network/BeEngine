package org.distril.beengine.world.chunk.bitarray;

import com.google.common.base.Preconditions;

public class Pow2BitArray extends BitArray {

	public Pow2BitArray(Version version, int size, int[] words) {
		super(version, size, words);
	}

	@Override
	public void set(int index, int value) {
		Preconditions.checkElementIndex(index, this.size);
		Preconditions.checkArgument(value >= 0 && value <= this.version.getMaxEntryValue(), "Max value: %s. Received value", this.version.getMaxEntryValue(), value);

		var bitIndex = index * this.version.getBits();
		var arrayIndex = bitIndex >> 5;
		var offset = bitIndex & 31;
		this.words[arrayIndex] = this.words[arrayIndex] & ~(this.version.getMaxEntryValue() << offset) | (value & this.version.getMaxEntryValue()) << offset;
	}

	@Override
	public int get(int index) {
		Preconditions.checkElementIndex(index, this.size);

		var bitIndex = index * this.version.getBits();
		var arrayIndex = bitIndex >> 5;
		var wordOffset = bitIndex & 31;
		return this.words[arrayIndex] >>> wordOffset & this.version.getMaxEntryValue();
	}
}
