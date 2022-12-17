package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.data.inventory.ContainerSlotType;
import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData;
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket;
import lombok.Getter;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.material.Material;
import org.distril.beengine.player.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class MoveItemStackAction extends ItemStackAction {

	private final int count;

	public MoveItemStackAction(StackRequestSlotInfoData from, StackRequestSlotInfoData to,
	                           ItemStackTransaction transaction, int count) {
		super(from, to, transaction);
		this.count = count;
	}

	@Override
	public boolean isValid(Player player) {
		if (player.isCreative() && this.getFrom().getContainer() == ContainerSlotType.CREATIVE_OUTPUT) {
			return true;
		}

		var fromItem = this.getFromItem();
		var toItem = this.getToItem();

		return fromItem.getCount() >= this.count &&
				(toItem.getMaterial() == Material.AIR || fromItem.equals(toItem, true, true));
	}

	@Override
	protected List<ItemStackResponsePacket.ContainerEntry> getContainers(Player player) {
		List<ItemStackResponsePacket.ContainerEntry> containers = new ArrayList<>();
		if (this.getFrom().getContainer() != ContainerSlotType.CREATIVE_OUTPUT) {
			containers.add(new ItemStackResponsePacket.ContainerEntry(
					this.getFrom().getContainer(),
					List.of(this.toNetwork(this.getFrom(), this.getFromInventory()))
			));
		}

		containers.add(new ItemStackResponsePacket.ContainerEntry(
				this.getTo().getContainer(),
				List.of(this.toNetwork(this.getTo(), this.getToInventory()))
		));

		return containers;
	}
}
