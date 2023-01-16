package org.distril.beengine.world.chunk.bitarray;

import lombok.Getter;

import java.util.Arrays;

@Getter
public abstract class BitArray {

	protected final Version version;
	protected final int size;
	protected final int[] words;

	public BitArray(Version version, int size, int[] words) {
		this.version = version;
		this.size = size;
		this.words = words;

		var expectedWordsLength = (int) Math.ceil((float) size / version.getEntriesPerWord());
		if (words.length != expectedWordsLength) {
			throw new IllegalArgumentException("Invalid length given for storage, got: " + words.length + " but expected: " + expectedWordsLength);
		}
	}

	public abstract void set(int index, int value);

	public abstract int get(int index);

	@Getter
	public enum Version {

		V16(16, 2, null),
		V8(8, 4, V16),
		V6(6, 5, V8), // 2 bit padding
		V5(5, 6, V6), // 2 bit padding
		V4(4, 8, V5),
		V3(3, 10, V4), // 2 bit padding
		V2(2, 16, V3),
		V1(1, 32, V2);

		private final byte bits;
		private final byte entriesPerWord;
		private final int maxEntryValue;
		private final Version next;

		Version(int bits, int entriesPerWord, Version next) {
			this.bits = (byte) bits;
			this.entriesPerWord = (byte) entriesPerWord;
			this.maxEntryValue = (1 << this.bits) - 1;
			this.next = next;
		}

		public static Version get(int version, boolean read) {
			return Arrays.stream(Version.values())
					.filter(ver -> (!read && ver.getEntriesPerWord() <= version) || (read && ver.getBits() == version))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Invalid palette version: " + version));
		}

		public BitArray createPalette(int size) {
			return this.createPalette(size, new int[this.getWordsForSize(size)]);
		}

		public int getWordsForSize(int size) {
			return (size / this.entriesPerWord) + (size % this.entriesPerWord == 0 ? 0 : 1);
		}

		public BitArray createPalette(int size, int[] words) {
			if (this == V3 || this == V5 || this == V6) {
				return new PaddedBitArray(this, size, words);
			} else {
				return new Pow2BitArray(this, size, words);
			}
		}
	}
}
