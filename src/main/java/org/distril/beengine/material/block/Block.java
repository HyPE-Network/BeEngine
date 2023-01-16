package org.distril.beengine.material.block;

import com.nukkitx.math.vector.Vector3i;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.material.Material;
import org.distril.beengine.world.World;

@Getter
@Setter
public abstract class Block implements Cloneable, BlockBehavior {

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
}
