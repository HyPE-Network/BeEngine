package org.distril.beengine.inventory.transaction.action

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket.ContainerEntry
import org.distril.beengine.inventory.Inventory
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player
import org.distril.beengine.util.Utils.getLogger

abstract class ItemStackAction(
	protected val from: StackRequestSlotInfoData? = null,
	protected val to: StackRequestSlotInfoData? = null,
	protected val transaction: ItemStackTransaction
) {

	protected open var fromItem = this.from.getItem()
		get() = field?.clone()
		set(value) {
			this.from?.getInventory()?.setItem(this.from.slot.toInt(), value, false)
		}

	protected var toItem = this.to.getItem()
		get() = field?.clone()
		set(value) {
			this.to?.getInventory()?.setItem(this.to.slot.toInt(), value, false)
		}

	abstract fun isValid(player: Player): Boolean

	abstract fun execute(player: Player): Boolean

	protected abstract fun getContainers(player: Player): List<ContainerEntry>

	fun onExecuteSuccess(player: Player) {
		this.from?.getInventory()?.sendSlots(player)
		this.to?.getInventory()?.sendSlots(player)

		this.transaction.status = ItemStackResponsePacket.ResponseStatus.OK
		this.transaction.addContainers(this.getContainers(player))
	}

	fun onExecuteFail(player: Player) {
		log.debug("Failed on transaction action: ${this.javaClass.simpleName}")

		this.transaction.status = ItemStackResponsePacket.ResponseStatus.ERROR
		this.transaction.addContainers(this.getContainers(player))
	}

	protected fun StackRequestSlotInfoData.toNetwork(): ItemStackResponsePacket.ItemEntry {
		val item = this.getItem() ?: return ItemStackResponsePacket.ItemEntry(
			0, 0, 0,
			0, "", 0
		)
		return ItemStackResponsePacket.ItemEntry(
			this.slot,
			this.slot,
			item.count.toByte(),
			item.networkId,
			item.customName ?: "",
			0 // todo
		)
	}

	private fun StackRequestSlotInfoData?.getItem(): Item? {
		if (this == null) return null
		return this.getInventory()?.getItem(this.slot.toInt())
	}

	private fun StackRequestSlotInfoData?.getInventory(): Inventory? {
		if (this == null) return null
		return transaction.getInventoryByType(this.container)
	}

	companion object {

		private val log = ItemStackAction.getLogger()
	}
}
