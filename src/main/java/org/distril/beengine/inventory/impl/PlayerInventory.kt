package org.distril.beengine.inventory.impl

import com.nukkitx.math.vector.Vector3i
import com.nukkitx.protocol.bedrock.data.inventory.ContainerId
import com.nukkitx.protocol.bedrock.packet.ContainerOpenPacket
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player

class PlayerInventory(player: Player) : CreatureInventory(player, ContainerId.INVENTORY) {

	override val holder = super.holder as Player

	val cursorInventory = PlayerCursorInventory(player)
	val craftingInventory = PlayerCraftingInventory(player)

	override fun clear() {
		super.clear()

		this.cursorInventory.clear()
	}

	fun getCursorItem() = this.cursorInventory.getItem(0)

	fun setCursorItem(item: Item?) = cursorInventory.setItem(0, item)

	override fun onOpen(player: Player) {
		val packet = ContainerOpenPacket()
		packet.id = this.id.toByte()
		packet.type = this.type.containerType
		packet.blockPosition = Vector3i.ZERO
		packet.uniqueEntityId = player.id

		player.sendPacket(packet)

		this.sendSlots(player)
	}

	override fun sendSlots(player: Player) {
		super.sendSlots(player)

		this.cursorInventory.sendSlots(player)
	}
}
