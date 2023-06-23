package org.distril.beengine.inventory.transaction.action

import com.nukkitx.protocol.bedrock.data.inventory.ContainerSlotType
import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket.ContainerEntry
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.material.Material
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player

abstract class MoveItemStackAction(
	from: StackRequestSlotInfoData,
	to: StackRequestSlotInfoData?,
	transaction: ItemStackTransaction,
	protected val count: Int,
	private val requestId: Int
) : ItemStackAction(from, to, transaction) {

	@Suppress("SetterBackingFieldAssignment")
	override var fromItem: Item? =
		(if (this.requestId == from.stackNetworkId) this.transaction.creativeOutput else super.fromItem).apply {
			this?.count = count
		}
		get() = field?.clone()
		set(value) {
			super.fromItem = value
		}

	override fun isValid(player: Player): Boolean {
		if (player.isCreative && this.from?.container === ContainerSlotType.CREATIVE_OUTPUT) return true

		val fromItem = this.fromItem!!
		val toItem = this.toItem!!
		return fromItem.count == count && (toItem.material === Material.AIR || fromItem.equal(toItem))
	}

	override fun getContainers(player: Player): List<ContainerEntry> {
		val containers = mutableListOf<ContainerEntry>()
		if (this.from?.container != ContainerSlotType.CREATIVE_OUTPUT) {
			containers.add(
				ContainerEntry(
					this.from?.container,
					listOf(this.from?.toNetwork())
				)
			)
		}

		containers.add(
			ContainerEntry(
				this.to?.container,
				listOf(this.to?.toNetwork())
			)
		)

		return containers
	}
}
