package org.distril.beengine.entity;

import org.distril.beengine.inventory.InventoryHolder;
import org.distril.beengine.inventory.impl.CreatureInventory;
import org.distril.beengine.player.Player;

public abstract class EntityCreature extends Entity implements InventoryHolder {

	private CreatureInventory inventory;

	public EntityCreature(EntityType type) {
		super(type);

		if (!(this instanceof Player)) {
			this.inventory = new CreatureInventory(this);
		}
	}

	@Override
	public CreatureInventory getInventory() {
		return this.inventory;
	}
}
