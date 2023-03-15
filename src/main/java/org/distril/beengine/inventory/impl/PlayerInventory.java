package org.distril.beengine.inventory.impl;

import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.data.inventory.ContainerId;
import com.nukkitx.protocol.bedrock.packet.ContainerOpenPacket;
import lombok.Getter;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;

@Getter
public class PlayerInventory extends CreatureInventory {

	private final PlayerCursorInventory cursorInventory;

	private final PlayerCraftingInventory craftingInventory;

	public PlayerInventory(Player player) {
		super(player, ContainerId.INVENTORY);

		this.cursorInventory = new PlayerCursorInventory(player);
		this.craftingInventory = new PlayerCraftingInventory(player);
	}

	@Override
	public void clear() {
		super.clear();

		this.cursorInventory.clear();
	}

	public Item getCursorItem() {
		return this.cursorInventory.getItem(0);
	}

	public void setCursorItem(Item item) {
		this.cursorInventory.setItem(0, item);
	}

	@Override
	protected void onOpen(Player player) {
		var packet = new ContainerOpenPacket();
		packet.setId((byte) this.getId());
		packet.setType(this.getType().getContainerType());
		packet.setBlockPosition(Vector3i.ZERO);
		packet.setUniqueEntityId(player.getId());

		player.sendPacket(packet);

		this.sendSlots(player);
	}

	@Override
	public void sendSlots(Player player) {
		super.sendSlots(player);

		this.cursorInventory.sendSlots(player);
	}

	@Override
	public Player getHolder() {
		return (Player) super.getHolder();
	}
}
