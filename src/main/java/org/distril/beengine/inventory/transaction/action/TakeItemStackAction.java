package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;

public class TakeItemStackAction extends MoveItemStackAction {

	public TakeItemStackAction(StackRequestSlotInfoData from, StackRequestSlotInfoData to,
	                           ItemStackTransaction transaction, int count) {
		super(from, to, transaction, count);
	}

	@Override
	public boolean execute(Player player) {
		var fromItem = this.getFromItem();
		var toItem = this.getToItem();

		// double-click
		if (toItem.getMaterial() != Material.AIR) {
			// todo check count from and to items
			toItem.incrementCount(Math.min(fromItem.getCount(), this.getCount()));

			fromItem = Item.AIR;

			this.setToItem(toItem);
			this.setFromItem(fromItem);
			return true;
		}

		this.setFromItem(toItem);
		this.setToItem(fromItem);
		return true;
	}
}
