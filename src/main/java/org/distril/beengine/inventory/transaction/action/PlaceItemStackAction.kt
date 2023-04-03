package org.distril.beengine.inventory.transaction.action

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.player.Player

class PlaceItemStackAction(
	from: StackRequestSlotInfoData,
	to: StackRequestSlotInfoData,
	transaction: ItemStackTransaction,
	count: Int,
	requestId: Int
) : MoveItemStackAction(from, to, transaction, count, requestId) {

	override fun execute(player: Player): Boolean {
		// todo add in stack
		this.fromItem = toItem
		this.toItem = fromItem
		return true
	}
}
