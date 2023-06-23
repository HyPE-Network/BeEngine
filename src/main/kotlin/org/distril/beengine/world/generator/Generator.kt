package org.distril.beengine.world.generator

import org.distril.beengine.world.chunk.Chunk
import java.util.concurrent.ThreadLocalRandom

fun interface Generator {

	fun generate(random: ThreadLocalRandom, chunk: Chunk)
}
