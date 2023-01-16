package org.distril.beengine.world;

import lombok.Getter;
import org.distril.beengine.Tickable;
import org.distril.beengine.entity.Entity;
import org.distril.beengine.world.chunk.ChunkManager;
import org.distril.beengine.world.generator.Generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class World extends Tickable {

	private final Path path;

	private final String worldName;
	private final Dimension dimension;
	private final Generator generator;

	private final ChunkManager chunkManager = new ChunkManager(this);
	private final Map<Long, Entity> entities = new HashMap<>();

	public World(String worldName, Dimension dimension, Generator generator) {
		super(worldName + " - world");
		this.path = Path.of("./worlds/" + worldName);

		try {
			Files.createDirectories(this.path);
		} catch (IOException ignored) {/**/}

		this.worldName = worldName;
		this.dimension = dimension;
		this.generator = generator;

		this.start();
	}

	@Override
	protected void onUpdate(long currentTick) {
		this.entities.values()
				.stream()
				.filter(Objects::nonNull)
				.forEach(entity -> entity.onUpdate(currentTick));
	}

	public void addEntity(Entity entity) {
		if (!this.entities.containsKey(entity.getId())) {
			this.entities.put(entity.getId(), entity);
		}
	}

	public void removeEntity(Entity entity) {
		this.entities.remove(entity.getId());
	}
}
