package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData;
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket;
import lombok.Getter;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.player.Player;

import java.util.List;

@Getter
public class DestroyItemStackAction extends ItemStackAction {

	private final int count;

	public DestroyItemStackAction(StackRequestSlotInfoData from, int count, ItemStackTransaction transaction) {
		super(from, null, transaction);
		this.count = count;
	}

	@Override
	public boolean isValid(Player player) {
		return this.count > 0 && player.isCreative();
	}

	@Override
	public boolean execute(Player player) {
		this.setFromItem(this.getFromItem().decrementCount(this.count));
		return true;
	}

	@Override
	protected List<ItemStackResponsePacket.ContainerEntry> getContainers(Player player) {
		return List.of(
				new ItemStackResponsePacket.ContainerEntry(this.getFrom().getContainer(),
						List.of(this.toNetwork(this.getFrom(), this.getFromInventory())))
		);
	}
}
