package org.distril.beengine.world.chunk;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.distril.beengine.material.block.BlockState;

import java.util.Arrays;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class SubChunk {

	public static final int VERSION = 8;
	public static final int COUNT = 16;

	private final Layer[] layers = {new Layer(), new Layer()};

	private final int index;


	public void setBlockState(int x, int y, int z, int layer, BlockState state) {
		this.layers[layer].set(x & 0xf, y & 0xf, z & 0xf, state);
	}

	public BlockState getBlockState(int x, int y, int z, int layer) {
		return this.layers[layer].get(x & 0xf, y & 0xf, z & 0xf);
	}

	public synchronized void writeToNetwork(ByteBuf buffer) {
		buffer.writeByte(VERSION);
		buffer.writeByte(this.layers.length);
		// buffer.writeByte(this.index);

		Arrays.stream(this.layers).forEach(layer -> layer.writeToNetwork(buffer));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		SubChunk that = (SubChunk) obj;
		return this.index == that.getIndex() && Arrays.equals(this.layers, that.getLayers());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.index, Arrays.hashCode(layers));
	}
}
