package org.distril.beengine.inventory.defaults;

import com.nukkitx.protocol.bedrock.data.inventory.ContainerId;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.InventoryType;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;

public class PlayerCursorInventory extends Inventory {

	public PlayerCursorInventory(Player player) {
		super(player, InventoryType.CURSOR, ContainerId.UI);
	}

	@Override
	public Item getItem(int slot) {
		return super.getItem(0);
	}

	@Override
	public boolean setItem(int slot, Item item) {
		return super.setItem(0, item);
	}

	@Override
	public void sendSlots(Player player) {
		this.sendSlot(0, player);
	}
}
