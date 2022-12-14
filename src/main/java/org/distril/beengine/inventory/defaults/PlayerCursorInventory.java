package org.distril.beengine.inventory.defaults;

import com.nukkitx.protocol.bedrock.data.inventory.ContainerId;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.InventoryType;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.ItemUtils;

public class PlayerCursorInventory extends Inventory {

	public PlayerCursorInventory(Player player) {
		super(player, InventoryType.CURSOR, ContainerId.INVENTORY);
	}

	public Item getCursorItem() {
		return this.getItem(0);
	}

	public void setCursorItem(Item item) {
		this.setItem(0, ItemUtils.getAirIfNull(item));
	}
}
