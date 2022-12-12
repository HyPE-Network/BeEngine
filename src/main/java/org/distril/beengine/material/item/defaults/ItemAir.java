package org.distril.beengine.material.item.defaults;

import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.Item;

public class ItemAir extends Item {

	public ItemAir() {
		super(Material.AIR);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}
