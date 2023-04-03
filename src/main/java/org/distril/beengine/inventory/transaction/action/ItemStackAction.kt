package org.distril.beengine.inventory.transaction.action

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket.ContainerEntry
import org.apache.logging.log4j.LogManager
import org.distril.beengine.inventory.Inventory
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player

abstract class ItemStackAction(
	protected val from: StackRequestSlotInfoData? = null,
	protected val to: StackRequestSlotInfoData? = null,
	protected val transaction: ItemStackTransaction
) {

	protected open var fromItem: Item?
		get() = this.from.getItem()?.clone()
		set(value) {
			this.fromInventory.setItem(this.from!!.slot.toInt(), value, false)
		}

	protected var toItem: Item?
		get() = this.to.getItem()?.clone()
		set(value) {
			this.toInventory.setItem(this.to!!.slot.toInt(), value, false)
		}

	abstract fun isValid(player: Player): Boolean

	abstract fun execute(player: Player): Boolean

	protected abstract fun getContainers(player: Player): List<ContainerEntry>

	fun onExecuteSuccess(player: Player) {
		this.fromInventory.sendSlots(player)
		this.toInventory.sendSlots(player)

		this.transaction.status = ItemStackResponsePacket.ResponseStatus.OK
		this.transaction.addContainers(this.getContainers(player))
	}

	fun onExecuteFail(player: Player) {
		log.debug("Failed on transaction action: ${this.javaClass.simpleName}")

		this.transaction.status = ItemStackResponsePacket.ResponseStatus.ERROR
		this.transaction.addContainers(this.getContainers(player))
	}

	private val fromInventory: Inventory
		get() = this.from.getInventory()

	private val toInventory: Inventory
		get() = this.to.getInventory()

	protected fun StackRequestSlotInfoData.toNetwork(): ItemStackResponsePacket.ItemEntry {
		val item = this.getItem()!!
		val durability = 0 // todo
		return ItemStackResponsePacket.ItemEntry(
			this.slot,
			this.slot,
			item.count.toByte(),
			item.networkId,
			item.customName ?: "",
			durability
		)
	}

	private fun StackRequestSlotInfoData?.getInventory(): Inventory {
		return transaction.getInventoryByType(this!!.container)
	}

	private fun StackRequestSlotInfoData?.getItem(): Item? {
		if (this == null) return null

		val inventoryByType = transaction.getInventoryByType(this.container)

		return inventoryByType.getItem(this.slot.toInt())
	}

	companion object {

		private val log = LogManager.getLogger(ItemStackTransaction::class.java)
	}
}
