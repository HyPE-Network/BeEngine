package org.distril.beengine.inventory.impl

import com.nukkitx.protocol.bedrock.data.inventory.ContainerId
import org.distril.beengine.inventory.Inventory
import org.distril.beengine.inventory.InventoryType
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player

class PlayerCursorInventory(player: Player) : Inventory(player, InventoryType.CURSOR, ContainerId.UI) {

	override fun getItem(slot: Int) = super.getItem(0)

	override fun setItem(slot: Int, item: Item?, send: Boolean): Boolean {
		return super.setItem(0, item, send)
	}

	override fun sendSlot(slot: Int, vararg players: Player) = super.sendSlot(0, *players)

	override fun sendSlots(player: Player) = this.sendSlot(0, player)
}
