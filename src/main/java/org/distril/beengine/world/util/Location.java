package org.distril.beengine.world.util;

import com.nukkitx.math.vector.Vector3f;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.distril.beengine.world.World;
import org.distril.beengine.world.chunk.Chunk;

import java.util.Objects;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Location {

	private final Vector3f position;
	private final World world;

	public static Location from(Vector3f position, World world) {
		return new Location(position, world);
	}

	public static Location from(World world) {
		return Location.from(Vector3f.ZERO, world);
	}

	public float getX() {
		return this.position.getX();
	}

	public float getY() {
		return this.position.getY();
	}

	public float getZ() {
		return this.position.getZ();
	}

	public int getFloorX() {
		return this.position.getFloorX();
	}

	public int getFloorY() {
		return this.position.getFloorY();
	}

	public int getFloorZ() {
		return this.position.getFloorZ();
	}

	public Chunk getChunk() {
		return this.world.getChunk(this.getChunkX(), this.getChunkZ());
	}

	public int getChunkX() {
		return this.getFloorX() >> 4;
	}

	public int getChunkZ() {
		return this.getFloorZ() >> 4;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		Location that = (Location) obj;
		return this.position.equals(that.getPosition()) && this.world.equals(that.getWorld());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.position, this.world);
	}
}
