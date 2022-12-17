package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.player.Player;

public class PlaceItemStackAction extends MoveItemStackAction {

	public PlaceItemStackAction(StackRequestSlotInfoData from, StackRequestSlotInfoData to,
	                            ItemStackTransaction transaction, int count) {
		super(from, to, transaction, count);
	}

	@Override
	public boolean execute(Player player) {
		var fromItem = this.getFromItem();
		var toItem = this.getToItem();

		// todo add in stack

		this.setFromItem(toItem);
		this.setToItem(fromItem);
		return true;
	}
}
