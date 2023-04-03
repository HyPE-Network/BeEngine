package org.distril.beengine.inventory.transaction.action

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket.ContainerEntry
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.player.Player

class DestroyItemStackAction(
	from: StackRequestSlotInfoData,
	private val count: Int,
	transaction: ItemStackTransaction
) : ItemStackAction(from = from, transaction = transaction) {

	override fun isValid(player: Player) = this.count > 0 && player.isCreative

	override fun execute(player: Player): Boolean {
		val fromItem = fromItem
		fromItem!!.count = fromItem.count - count

		this.fromItem = fromItem
		return true
	}

	override fun getContainers(player: Player): List<ContainerEntry> {
		return listOf(
			ContainerEntry(
				this.from!!.container,
				listOf(this.from.toNetwork())
			)
		)
	}
}
