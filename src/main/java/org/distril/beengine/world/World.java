package org.distril.beengine.world;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.packet.UpdateBlockPacket;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.Tickable;
import org.distril.beengine.entity.Entity;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.block.Block;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;
import org.distril.beengine.server.Server;
import org.distril.beengine.util.Direction;
import org.distril.beengine.world.chunk.Chunk;
import org.distril.beengine.world.chunk.ChunkManager;
import org.distril.beengine.world.generator.Generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Log4j2
public class World extends Tickable {

	private static final int MAX_Y = 256;
	private static final int MIN_Y = 0;

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

	public Chunk getChunk(Vector3i position) {
		return this.chunkManager.getChunk(position.getX() >> 4, position.getZ() >> 4);
	}

	public Chunk getChunk(int x, int z) {
		return this.chunkManager.getChunk(x, z);
	}

	public Chunk getChunk(long key) {
		return this.chunkManager.getChunk(key);
	}

	public Block getLoadedBlock(Vector3i position) {
		return this.getLoadedBlock(position.getX(), position.getY(), position.getZ());
	}

	public Block getLoadedBlock(int x, int y, int z) {
		return this.getLoadedBlock(x, y, z, 0);
	}

	public Block getLoadedBlock(int x, int y, int z, int layer) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		var chunk = this.getLoadedChunk(chunkX, chunkZ);

		if (MIN_Y < 0 || y >= MAX_Y) {
			var block = Material.AIR.getBlock();
			block.setPosition(Vector3i.from(x, y, z));
			return block;
		}

		if (chunk == null) {
			return null;
		}

		var block = chunk.getBlock(x & 0xf, y, z & 0xf, layer);
		block.setPosition(this, Vector3i.from(x, y, z));
		return block;
	}

	public Block getBlock(Vector3i position) {
		return this.getBlock(position.getX(), position.getY(), position.getZ());
	}

	public Block getBlock(int x, int y, int z) {
		return this.getBlock(x, y, z, 0);
	}

	public Block getBlock(int x, int y, int z, int layer) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		var chunk = this.getChunk(chunkX, chunkZ);

		if (MIN_Y < 0 || y >= MAX_Y) {
			var block = Material.AIR.getBlock();
			block.setPosition(Vector3i.from(x, y, z));
			return block;
		}

		if (chunk == null) {
			return null;
		}

		var block = chunk.getBlock(x & 0xf, y, z & 0xf, layer);
		block.setPosition(this, Vector3i.from(x, y, z));
		return block;
	}

	public void setBlock(Vector3i position, int layer, Block block) {
		this.setBlock(position.getX(), position.getY(), position.getZ(), layer, block, true);
	}

	public void setBlock(int x, int y, int z, int layer, Block block) {
		this.setBlock(x, y, z, layer, block, true);
	}

	public void setBlock(int x, int y, int z, int layer, Block block, boolean send) {
		if (y < MIN_Y || y >= MAX_Y) {
			return;
		}

		var chunk = this.getChunk(x >> 4, z >> 4);
		chunk.setBlock(x & 0xF, y, z & 0xF, layer, block);

		if (send) {
			this.sendBlocks(chunk.getPlayers(), Collections.singleton(block), UpdateBlockPacket.FLAG_ALL_PRIORITY);
		}
	}

	public void sendBlocks(Collection<Player> targets, Collection<Block> blocks, Set<UpdateBlockPacket.Flag> flags) {
		blocks.stream().filter(Objects::isNull).forEach(block -> {
			throw new NullPointerException("Null block is update array");
		});

		List<UpdateBlockPacket> packets = new ArrayList<>();

		blocks.forEach(block -> {
			var packet = new UpdateBlockPacket();
			packet.getFlags().addAll(flags);
			packet.setBlockPosition(block.getPosition());
			packet.setRuntimeId(block.getState().getRuntimeId());
			packet.setDataLayer(0);

			packets.add(packet);

			packet.setDataLayer(1);
			packets.add(packet);
		});

		Server.getInstance().broadcastPackets(targets, packets);
	}

	public Item useItemOn(Vector3i blockPosition, Item item, Direction blockFace, Vector3f clickPosition, Player player) {
		return this.useItemOn(blockPosition, item, blockFace, clickPosition, player, true);
	}

	public Item useItemOn(Vector3i blockPosition, Item item, Direction blockFace, Vector3f clickPosition, Player player, boolean playSound) {
		var target = this.getBlock(blockPosition);
		var block = target.getSide(blockFace);

		Vector3i blockPos = block.getPosition();

		if (blockPos.getY() >= MAX_Y || blockPos.getY() < MIN_Y) {
			return null;
		}

		if (target.getMaterial() == Material.AIR) {
			return null;
		}

		this.setBlock(blockPos, 0, target);

		return item;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		World that = (World) obj;
		return this.path.equals(that.getPath()) && this.worldName.equals(that.getWorldName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.path, this.worldName);
	}
}
