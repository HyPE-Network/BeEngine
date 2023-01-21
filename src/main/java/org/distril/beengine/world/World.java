package org.distril.beengine.world;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.Tickable;
import org.distril.beengine.entity.Entity;
import org.distril.beengine.world.chunk.Chunk;
import org.distril.beengine.world.chunk.ChunkManager;
import org.distril.beengine.world.generator.Generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Log4j2
public class World extends Tickable {

	private final Path path;

	private final String worldName;
	private final Dimension dimension;
	private final Generator generator;

	private final ChunkManager chunkManager = new ChunkManager(this);
	private final Map<Long, Entity> entities = new ConcurrentHashMap<>();

	public World(String worldName, Dimension dimension, Generator generator) {
		super(worldName + " - World");
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
		this.addEntity(entity, false);
	}

	public void addEntity(Entity entity, boolean addInChunk) {
		this.entities.put(entity.getId(), entity);

		if (addInChunk) {
			var location = entity.getLocation();

			location.getChunk().addEntity(entity);
		}
	}

	public void removeEntity(Entity entity) {
		this.entities.remove(entity.getId());
	}

	public void removeEntity(Entity entity, boolean removeInChunk) {
		this.entities.remove(entity.getId());

		if (removeInChunk) {
			var location = entity.getLocation();

			location.getChunk().removeEntity(entity);
		}
	}

	public Chunk getLoadedChunk(int x, int z) {
		return this.chunkManager.getLoadedChunk(x, z);
	}

	public Chunk getLoadedChunk(long key) {
		return this.chunkManager.getLoadedChunk(key);
	}

	public Chunk getChunk(int x, int z) {
		return this.chunkManager.getChunk(x, z);
	}

	public Chunk getChunk(long key) {
		return this.chunkManager.getChunk(key);
	}
}
