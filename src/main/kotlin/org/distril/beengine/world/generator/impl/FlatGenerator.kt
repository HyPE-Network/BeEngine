package org.distril.beengine.world.generator.impl

import org.distril.beengine.material.Material
import org.distril.beengine.world.chunk.Chunk
import org.distril.beengine.world.generator.Generator
import java.util.concurrent.ThreadLocalRandom

class FlatGenerator : Generator {

	override fun generate(random: ThreadLocalRandom, chunk: Chunk) {
		for (x in 0..15) for (z in 0..15) {
			chunk.setBlock(x, 0, z, Material.BEDROCK.getBlock())
			chunk.setBlock(x, 1, z, Material.DIRT.getBlock())
			chunk.setBlock(x, 2, z, Material.DIRT.getBlock())
			chunk.setBlock(x, 3, z, Material.GRASS.getBlock())
		}
	}
}
