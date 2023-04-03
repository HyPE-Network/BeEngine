package org.distril.beengine.inventory.transaction.action

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.material.Material
import org.distril.beengine.player.Player
import kotlin.math.min

class TakeItemStackAction(
	from: StackRequestSlotInfoData,
	to: StackRequestSlotInfoData,
	transaction: ItemStackTransaction,
	count: Int,
	requestId: Int
) : MoveItemStackAction(from, to, transaction, count, requestId) {

	override fun execute(player: Player): Boolean {
		var fromItem = this.fromItem
		val toItem = this.toItem!!

		// double-click
		if (toItem.material != Material.AIR) {
			// todo check count from and to items
			toItem.count = toItem.count + min(fromItem!!.count, this.count)
			fromItem = null
		}

		this.fromItem = toItem
		this.toItem = fromItem
		return true
	}
}
