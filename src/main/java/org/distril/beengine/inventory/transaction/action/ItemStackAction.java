package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData;
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;

import java.util.List;

@Log4j2
@Getter(value = AccessLevel.PROTECTED)
public abstract class ItemStackAction {

	private final StackRequestSlotInfoData from, to;
	private final ItemStackTransaction transaction;
	private final Item fromItem, toItem;

	public ItemStackAction(StackRequestSlotInfoData from, StackRequestSlotInfoData to, ItemStackTransaction transaction) {
		this.from = from;
		this.to = to;
		this.transaction = transaction;

		if (this.from != null) {
			this.fromItem = this.getItem(this.getFromInventory(), from.getSlot());
		} else {
			this.fromItem = null;
		}

		if (to != null) {
			this.toItem = this.getItem(this.getToInventory(), to.getSlot());
		} else {
			this.toItem = null;
		}
	}

	public abstract boolean isValid(Player player);

	public abstract boolean execute(Player player);

	protected abstract List<ItemStackResponsePacket.ContainerEntry> getContainers(Player player);

	public void onExecuteSuccess(Player player) {
		Inventory inventory;
		if ((inventory = this.getFromInventory()) != null) {
			inventory.sendSlots(player);
		}

		if ((inventory = this.getToInventory()) != null) {
			inventory.sendSlots(player);
		}

		this.transaction.setStatus(ItemStackResponsePacket.ResponseStatus.OK);
		this.transaction.addContainers(this.getContainers(player));
	}

	public void onExecuteFail(Player player) {
		log.debug("Failed on transaction action: {}", this.getClass().getSimpleName());

		this.transaction.setStatus(ItemStackResponsePacket.ResponseStatus.ERROR);
		this.transaction.addContainers(this.getContainers(player));
	}

	private Item getItem(Inventory inventory, int slot) {
		if (inventory != null) {
			return inventory.getItem(slot);
		}

		return Item.AIR;
	}

	protected void setFromItem(Item item) {
		this.getFromInventory().setItem(this.from.getSlot(), item, false);
	}

	protected void setToItem(Item item) {
		this.getToInventory().setItem(this.to.getSlot(), item, false);
	}

	protected Inventory getFromInventory() {
		if (this.from != null) {
			return this.transaction.getInventoryByType(this.from.getContainer());
		}

		return null;
	}

	protected Inventory getToInventory() {
		if (this.to != null) {
			return this.transaction.getInventoryByType(this.to.getContainer());
		}

		return null;
	}

	protected ItemStackResponsePacket.ItemEntry toNetwork(StackRequestSlotInfoData data, Inventory inventory) {
		var item = inventory.getItem(data.getSlot());
		int durablility = 0; // todo

		return new ItemStackResponsePacket.ItemEntry(data.getSlot(),
				data.getSlot(),
				(byte) item.getCount(),
				item.getNetworkId(),
				item.getCustomName() == null ? "" : item.getCustomName(),
				durablility);

	}
}
