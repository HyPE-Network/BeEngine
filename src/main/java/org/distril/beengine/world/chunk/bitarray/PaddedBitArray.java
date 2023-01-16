package org.distril.beengine.world.chunk.bitarray;

import com.google.common.base.Preconditions;

public class PaddedBitArray extends BitArray {

	public PaddedBitArray(Version version, int size, int[] words) {
		super(version, size, words);
	}

	@Override
	public void set(int index, int value) {
		Preconditions.checkElementIndex(index, this.size);
		Preconditions.checkArgument(value >= 0 && value <= this.version.getMaxEntryValue(), "Max value: %s. Received value", this.version.getMaxEntryValue(), value);

		var arrayIndex = index / this.version.getEntriesPerWord();
		var offset = (index % this.version.getEntriesPerWord()) * this.version.getBits();
		this.words[arrayIndex] = this.words[arrayIndex] & ~(this.version.getMaxEntryValue() << offset) | (value & this.version.getMaxEntryValue()) << offset;
	}

	@Override
	public int get(int index) {
		Preconditions.checkElementIndex(index, this.size);

		var arrayIndex = index / this.version.getEntriesPerWord();
		var offset = (index % this.version.getEntriesPerWord()) * this.version.getBits();
		return (this.words[arrayIndex] >>> offset) & this.version.getMaxEntryValue();
	}
}
