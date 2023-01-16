package org.distril.beengine.world.generator.impl;

import org.distril.beengine.material.Material;
import org.distril.beengine.world.chunk.Chunk;
import org.distril.beengine.world.generator.Generator;

import java.util.concurrent.ThreadLocalRandom;

public class FlatGenerator implements Generator {

	@Override
	public void generate(ThreadLocalRandom random, Chunk chunk) {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				chunk.setBlock(x, 0 + 50, z, 0, Material.BEDROCK.getBlock());
				chunk.setBlock(x, 1 + 50, z, 0, Material.DIRT.getBlock());
				chunk.setBlock(x, 2 + 50, z, 0, Material.DIRT.getBlock());
				chunk.setBlock(x, 3 + 50, z, 0, Material.GRASS.getBlock());
			}
		}
	}
}
