package org.distril.beengine.entity;

import org.distril.beengine.inventory.InventoryHolder;
import org.distril.beengine.inventory.impl.CreatureInventory;
import org.distril.beengine.player.Player;
import org.distril.beengine.world.util.Location;

public abstract class EntityCreature extends Entity implements InventoryHolder {

	private CreatureInventory inventory;

	public EntityCreature(EntityType type, Location location) {
		super(type, location);

		if (!(this instanceof Player)) {
			this.inventory = new CreatureInventory(this);
		}
	}

	@Override
	public CreatureInventory getInventory() {
		return this.inventory;
	}
}
