package org.distril.beengine.material.block.impl;

import org.distril.beengine.material.Material;
import org.distril.beengine.material.block.Block;
import org.distril.beengine.material.block.BlockState;

public class NoopBlock extends Block {

	public NoopBlock(Material material) {
		super(material);
	}

	public NoopBlock(Material material, BlockState state) {
		super(material, state);
	}
}
