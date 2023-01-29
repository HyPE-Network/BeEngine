package org.distril.beengine.util;

public class ChunkUtils {

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
