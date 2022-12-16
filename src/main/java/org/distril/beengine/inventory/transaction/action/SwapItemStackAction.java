package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData;
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket;
import org.distril.beengine.player.Player;

import java.util.List;

public class SwapItemStackAction extends ItemStackAction {

	public SwapItemStackAction(StackRequestSlotInfoData from, StackRequestSlotInfoData to) {
		super(from, to);
	}

	@Override
	public boolean isValid(Player player) {
		return !this.getFromItem().equals(this.getToItem(), true, false, true);
	}

	@Override
	public boolean execute(Player player) {
		var fromItem = this.getFromItem();
		var toItem = this.getToItem();

		this.getFromInventory().setItem(this.getFromSlot(), toItem, false);
		this.getToInventory().setItem(this.getToSlot(), fromItem, false);

		return true;
	}

	@Override
	protected List<ItemStackResponsePacket.ContainerEntry> getContainers(Player player) {
		return List.of(
				new ItemStackResponsePacket.ContainerEntry(this.getFrom().getContainer(),
						List.of(this.toNetwork(this.getFrom(), this.getFromInventory()))),
				new ItemStackResponsePacket.ContainerEntry(this.getTo().getContainer(),
						List.of(this.toNetwork(this.getTo(), this.getToInventory())))
		);
	}
}
