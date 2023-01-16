package org.distril.beengine.material.block;

import org.distril.beengine.material.Material;
import org.distril.beengine.material.block.impl.NoopBlock;

import java.util.EnumMap;
import java.util.Map;

public class BlockRegistry {

	private final Map<Material, Block> byMaterial = new EnumMap<>(Material.class);

	public void init() {
		// todo
	}

	public void register(Material material, Block block) {
		this.byMaterial.put(material, block);
	}

	public Block getBlockFromState(BlockState state) {
		var states = state.getStates();

		var identifier = states.getString("name");

		var material = Material.fromIdentifier(identifier);
		return this.from(material, state);
	}

	@SuppressWarnings("unchecked")
	public <T extends Block> T from(Material material) {
		return (T) this.byMaterial.getOrDefault(material, new NoopBlock(material)).clone();
	}

	@SuppressWarnings("unchecked")
	public <T extends Block> T from(Material material, BlockState state) {
		var from = this.from(material);
		from.setState(state);
		return (T) from;
	}
}
