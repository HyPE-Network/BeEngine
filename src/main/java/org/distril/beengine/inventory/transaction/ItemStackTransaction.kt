package org.distril.beengine.inventory.transaction

import com.nukkitx.protocol.bedrock.data.inventory.ContainerSlotType
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket.ContainerEntry
import org.apache.logging.log4j.LogManager
import org.distril.beengine.inventory.transaction.action.ItemStackAction
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player
import java.util.*

class ItemStackTransaction(val player: Player) {

	private val containers: MutableMap<ContainerSlotType, MutableList<ItemStackResponsePacket.ItemEntry>> =
		EnumMap(ContainerSlotType::class.java)

	var creativeOutput: Item? = null

	var status = ItemStackResponsePacket.ResponseStatus.OK

	fun handle(action: ItemStackAction): Boolean {
		if (!action.isValid(this.player)) {
			log.warn("Failed validation check on ${action.javaClass.simpleName}")
			action.onExecuteFail(player)
			return false
		}

		if (action.execute(player)) {
			action.onExecuteSuccess(player)
			return true
		}

		action.onExecuteFail(player)
		return false
	}

	fun clear() = this.containers.clear()

	fun addContainers(containers: List<ContainerEntry>) {
		containers.forEach {
			this.containers.computeIfAbsent(it.container) { mutableListOf() }.apply { this.addAll(it.items) }
		}
	}

	val containersEntries: List<ContainerEntry>
		get() = this.containers.keys.map { ContainerEntry(it, this.containers[it]) }

	fun getInventoryByType(type: ContainerSlotType) = when (type) {
		ContainerSlotType.HOTBAR,
		ContainerSlotType.HOTBAR_AND_INVENTORY,
		ContainerSlotType.INVENTORY,
		ContainerSlotType.OFFHAND -> player.inventory

		ContainerSlotType.CURSOR -> player.inventory.cursorInventory
		else -> player.openedInventory!!
	}

	companion object {

		private val log = LogManager.getLogger(ItemStackTransaction::class.java)
	}
}
