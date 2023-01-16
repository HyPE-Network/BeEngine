package org.distril.beengine.world.chunk;

import com.nukkitx.network.VarInts;
import io.netty.buffer.ByteBuf;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.block.BlockState;
import org.distril.beengine.world.chunk.bitarray.BitArray;
import org.distril.beengine.world.chunk.bitarray.BitArray.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
public class Layer {

	private static final BlockState AIR_STATE = Material.AIR.getBlock().getState();

	private static final int SIZE = 4096;

	private final List<BlockState> palette = new ArrayList<>(16);
	private BitArray bitArray;

	public Layer() {
		this(Version.V2);
	}

	public Layer(Version version) {
		this.bitArray = version.createPalette(SIZE);
		this.palette.add(AIR_STATE);
	}

	private static Version getVersionFromHeader(byte header) {
		return Version.get(header >> 1, true);
	}

	private int getPaletteHeader(Version version, boolean runtime) {
		return (version.getBits() << 1) | (runtime ? 1 : 0);
	}

	public BlockState get(int x, int y, int z) {
		var index = (x << 8) | (z << 4) | y;
		return this.palette.get(this.bitArray.get(index));
	}

	public void set(int x, int y, int z, BlockState state) {
		try {
			var index = (x << 8) | (z << 4) | y;
			var idx = this.idFor(state);
			this.bitArray.set(index, idx);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Unable to set block state: " + state + ", palette: " + palette, exception);
		}
	}

	public void writeToNetwork(ByteBuf buffer) {
		buffer.writeByte(this.getPaletteHeader(this.bitArray.getVersion(), true));

		for (int word : this.bitArray.getWords()) {
			buffer.writeIntLE(word);
		}

		VarInts.writeInt(buffer, this.palette.size());

		this.palette.forEach(state -> VarInts.writeInt(buffer, state.getRuntimeId()));
	}

	private void onResize(Version version) {
		var newBitArray = version.createPalette(SIZE);

		for (int index = 0; index < SIZE; index++) {
			newBitArray.set(index, this.bitArray.get(index));
		}

		this.bitArray = newBitArray;
	}

	private int idFor(BlockState state) {
		var index = this.palette.indexOf(state);
		if (index == -1) {
			index = this.palette.size();
			var version = this.bitArray.getVersion();
			if (index > version.getMaxEntryValue()) {
				var next = version.getNext();
				if (next != null) {
					this.onResize(next);
				}
			}

			this.palette.add(state);
			return index;
		}

		return index;
	}

	public boolean isEmpty() {
		if (this.palette.size() != 1) {
			return Arrays.stream(this.bitArray.getWords()).noneMatch(word -> Integer.toUnsignedLong(word) != 0L);
		}

		return true;
	}
}
