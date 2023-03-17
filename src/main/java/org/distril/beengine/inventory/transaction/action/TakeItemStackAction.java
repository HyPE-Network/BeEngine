package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.material.Material;
import org.distril.beengine.player.Player;

public class TakeItemStackAction extends MoveItemStackAction {

	public TakeItemStackAction(StackRequestSlotInfoData from, StackRequestSlotInfoData to,
	                           ItemStackTransaction transaction, int count, int requestId) {
		super(from, to, transaction, count, requestId);
	}

	@Override
	public boolean execute(Player player) {
		var fromItem = this.getFromItem();
		var toItem = this.getToItem();

		// double-click
		if (toItem.getMaterial() != Material.AIR) {
			// todo check count from and to items
			toItem.setCount(toItem.getCount() + Math.min(fromItem.getCount(), this.getCount()));

			fromItem = null;
		}

		this.setFromItem(toItem);
		this.setToItem(fromItem);
		return true;
	}
}
