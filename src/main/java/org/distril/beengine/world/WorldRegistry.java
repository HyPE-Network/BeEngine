package org.distril.beengine.world;

import org.distril.beengine.world.generator.impl.FlatGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldRegistry {

	private final Map<String, World> byName = new ConcurrentHashMap<>();

	private World defaultWorld;

	public void init() {
		// todo: load all worlds

		this.defaultWorld = new World("defaultWorld", Dimension.OVERWORLD, new FlatGenerator());
	}

	public World getDefaultWorld() {
		return this.defaultWorld;
	}
}
