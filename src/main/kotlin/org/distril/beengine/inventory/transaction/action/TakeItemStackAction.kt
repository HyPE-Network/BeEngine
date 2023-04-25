package org.distril.beengine.inventory.transaction.action

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.player.Player

class TakeItemStackAction(
	from: StackRequestSlotInfoData,
	to: StackRequestSlotInfoData,
	transaction: ItemStackTransaction,
	count: Int,
	requestId: Int
) : MoveItemStackAction(from, to, transaction, count, requestId) {

	override fun execute(player: Player): Boolean {
		this.fromItem = this.toItem
		this.toItem = this.fromItem
		return true
	}
}
