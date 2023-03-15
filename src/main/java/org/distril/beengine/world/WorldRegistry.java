package org.distril.beengine.world;

import org.distril.beengine.util.Utils;
import org.distril.beengine.world.generator.Generator;
import org.distril.beengine.world.generator.impl.FlatGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldRegistry {

	private static final String WORLD_PATH = "worlds";

	private final Map<String, World> byName = new ConcurrentHashMap<>();

	private World defaultWorld;

	public void init() {
		Utils.createDirectories(WORLD_PATH);

		// todo: load all worlds

		this.defaultWorld = this.createOrLoadWorld("defaultWorld", Dimension.OVERWORLD, new FlatGenerator());
	}

	public World createOrLoadWorld(String name, Dimension dimension, Generator generator) {
		var world = this.loadWorld(name);

		if (world == null) {
			world = this.createWorld(name, dimension, generator);
		}

		this.byName.put(name, world);
		return world;
	}

	public World loadWorld(String name) {
		return null; // todo
	}

	public World createWorld(String name, Dimension dimension, Generator generator) {
		Utils.createDirectories(WORLD_PATH, name);

		var world = new World(name, dimension, generator);

		this.byName.put(name, world);
		return world;
	}

	public World getWorld(String name) {
		return this.byName.get(name);
	}

	public World getDefaultWorld() {
		return this.defaultWorld;
	}
}
