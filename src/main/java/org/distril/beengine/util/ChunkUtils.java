package org.distril.beengine.util;


import com.google.common.base.Preconditions;

public class ChunkUtils {

	public static void checkBounds(int x, int y, int z) {
		Preconditions.checkElementIndex(x, 16, "X coordinate");
		Preconditions.checkElementIndex(y, 256, "Y coordinate");
		Preconditions.checkElementIndex(z, 16, "Z coordinate");
	}

	public static int fromKeyX(long key) {
		return (int) (key >> 32);
	}

	public static int fromKeyZ(long key) {
		return (int) key;
	}

	public static long key(int x, int z) {
		return (((long) x) << 32) | (z & 0xffffffffL);
	}
}
