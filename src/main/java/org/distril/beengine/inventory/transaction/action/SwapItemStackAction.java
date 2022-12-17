package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData;
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.player.Player;

import java.util.List;

public class SwapItemStackAction extends ItemStackAction {

	public SwapItemStackAction(StackRequestSlotInfoData from, StackRequestSlotInfoData to, ItemStackTransaction transaction) {
		super(from, to, transaction);
	}

	@Override
	public boolean isValid(Player player) {
		return !this.getFromItem().equals(this.getToItem(), true, false, true);
	}

	@Override
	public boolean execute(Player player) {
		this.setFromItem(this.getToItem());
		this.setToItem(this.getFromItem());
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
