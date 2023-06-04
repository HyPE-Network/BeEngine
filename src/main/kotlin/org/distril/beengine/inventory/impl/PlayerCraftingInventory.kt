package org.distril.beengine.inventory.impl

import com.nukkitx.protocol.bedrock.data.inventory.ContainerId
import org.distril.beengine.inventory.Inventory
import org.distril.beengine.inventory.InventoryType
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player

class PlayerCraftingInventory(holder: Player) : Inventory(holder, InventoryType.PLAYER, ContainerId.NONE) {

    override val holder = super.holder as Player

    override fun setItem(slot: Int, item: Item?, send: Boolean) = super.setItem(slot - SLOT_OFFSET, item, false)

    override fun getItem(slot: Int) = super.getItem(slot - SLOT_OFFSET)

    override fun sendSlot(slot: Int, vararg players: Player) = super.sendSlot(slot + SLOT_OFFSET, *players)

    companion object {

        private const val SLOT_OFFSET = 28
    }
}
