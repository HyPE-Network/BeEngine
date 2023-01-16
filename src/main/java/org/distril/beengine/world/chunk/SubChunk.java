package org.distril.beengine.world.chunk;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.material.block.BlockState;

import java.util.Arrays;

@Getter
@Log4j2
@ToString
public class SubChunk {

	public static final int VERSION = 8;
	public static final int COUNT = 16;

	private final int index;
	private final Layer[] layers;

	public SubChunk(int index) {
		this.index = index;
		this.layers = new Layer[]{
				new Layer(),
				new Layer()
		};
	}

	public void setBlockState(int x, int y, int z, int layer, BlockState state) {
		this.layers[layer].set(x & 0xf, y & 0xf, z & 0xf, state);
	}

	public BlockState getBlockState(int x, int y, int z, int layer) {
		return this.layers[layer].get(x & 0xf, y & 0xf, z & 0xf);
	}

	public void writeToNetwork(ByteBuf buffer) {
		buffer.writeByte(VERSION);
		buffer.writeByte(this.layers.length);
		// buffer.writeByte(this.index);

		Arrays.stream(this.layers).forEach(layer -> layer.writeToNetwork(buffer));
	}
}
