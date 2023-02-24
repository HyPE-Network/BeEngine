package org.distril.beengine.inventory.impl;

import com.nukkitx.protocol.bedrock.data.inventory.ContainerId;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.InventoryType;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;

public class PlayerCraftingInventory extends Inventory {

	private static final int SLOT_OFFSET = 28;

	@Setter
	@Getter
	private Item creativeOutput;

	public PlayerCraftingInventory(Player holder) {
		super(holder, InventoryType.PLAYER, ContainerId.NONE);
	}

	@Override
	public boolean setItem(int slot, Item item, boolean send) {
		return super.setItem(slot - SLOT_OFFSET, item, false);
	}

	@Override
	public Item getItem(int slot) {
		return super.getItem(slot - SLOT_OFFSET);
	}

	@Override
	protected void sendSlot(int slot, Player... players) {
		slot += SLOT_OFFSET;

		super.sendSlot(slot, players);
	}

	@Override
	public Player getHolder() {
		return (Player) super.getHolder();
	}
}
