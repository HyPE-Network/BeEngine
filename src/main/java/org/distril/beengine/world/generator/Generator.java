package org.distril.beengine.world.generator;


import org.distril.beengine.world.chunk.Chunk;

import java.util.concurrent.ThreadLocalRandom;

public interface Generator {

	void generate(ThreadLocalRandom random, Chunk chunk);
}
