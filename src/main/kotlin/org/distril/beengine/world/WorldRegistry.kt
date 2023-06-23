package org.distril.beengine.world

import org.distril.beengine.util.Utils
import org.distril.beengine.world.generator.Generator
import org.distril.beengine.world.generator.impl.FlatGenerator
import java.util.concurrent.ConcurrentHashMap

class WorldRegistry {

	private val byName: MutableMap<String, World> = ConcurrentHashMap()

	lateinit var defaultWorld: World
		private set

	fun init() {
		Utils.createDirectories(WORLD_PATH)

		// todo: load all worlds
		this.defaultWorld = this.createOrLoadWorld("defaultWorld", Dimension.OVERWORLD, FlatGenerator())
	}

	fun createOrLoadWorld(name: String, dimension: Dimension, generator: Generator): World {
		val world = this.loadWorld(name) ?: this.createWorld(name, dimension, generator)

		this.byName[name] = world
		return world
	}

	fun loadWorld(name: String): World? {
		return null // todo
	}

	fun createWorld(name: String, dimension: Dimension, generator: Generator): World {
		Utils.createDirectories(WORLD_PATH, name)

		val world = World(name, dimension, generator)
		this.byName[name] = world
		return world
	}

	fun getWorld(name: String) = this.byName[name]

	companion object {

		private const val WORLD_PATH = "worlds"
	}
}
