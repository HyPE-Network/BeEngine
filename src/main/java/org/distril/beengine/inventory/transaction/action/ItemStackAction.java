package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData;
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;

import java.util.List;

@Log4j2
@Getter
@RequiredArgsConstructor
public abstract class ItemStackAction {

	private final StackRequestSlotInfoData from, to;
	private ItemStackTransaction transaction;

	public void onAddToTransaction(ItemStackTransaction transaction) {
		this.transaction = transaction;
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

	protected Item getFromItem() {
		var inventory = this.getFromInventory();
		if (inventory != null) {
			return inventory.getItem(this.getFromSlot());
		}

		return Item.AIR;
	}

	protected Item getToItem() {
		var inventory = this.getToInventory();
		if (inventory != null) {
			return inventory.getItem(this.getToSlot());
		}

		return Item.AIR;
	}

	protected int getFromSlot() {
		return this.from.getSlot();
	}

	protected int getToSlot() {
		return this.to.getSlot();
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
