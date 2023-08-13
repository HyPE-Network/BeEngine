package org.distril.beengine.world.generator

import org.distril.beengine.world.chunk.Chunk
import java.util.*

fun interface Generator {

	fun generate(random: Random, chunk: Chunk)
}
