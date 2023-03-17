package org.distril.beengine.material.block;

import com.nukkitx.math.vector.Vector3i;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.material.Material;
import org.distril.beengine.util.Direction;
import org.distril.beengine.world.World;

import java.util.Objects;

@Getter
@Setter
public abstract class Block implements Cloneable, BlockBehaviors {

	private final Material material;

	private BlockState state;

	private World world;
	private Vector3i position;

	public Block(Material material) {
		this.material = material;
		this.state = BlockPalette.getDefaultState(material);
	}

	public Block(Material material, BlockState state) {
		this.material = material;
		this.state = state;
	}

	public void setPosition(World world, Vector3i position) {
		this.world = world;
		this.position = position;
	}

	public Block getSide(Direction face) {
		return this.getSide(face, 1);
	}

	public Block getSide(Direction face, int step) {
		return this.world.getBlock(face.getOffset(this.position, step));
	}

	@Override
	public Block clone() {
		try {
			Block clone = (Block) super.clone();
			clone.state = state;
			clone.world = world;
			clone.position = position;
			return clone;
		} catch (CloneNotSupportedException exception) {
			throw new AssertionError();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		Block that = (Block) obj;
		return this.material == that.getMaterial() && this.state.equals(that.getState());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.material, this.state);
	}
}
