package org.distril.beengine.inventory.defaults;

import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.InventoryType;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.ItemUtils;

public class PlayerCursorInventory extends Inventory {

	public PlayerCursorInventory(Player player) {
		super(player, InventoryType.CURSOR);
	}

	public void setCursor(Item item) {
		this.setItem(0, ItemUtils.getAirIfNull(item));
	}

	public Item getCursor() {
		return this.getItem(0);
	}
}
