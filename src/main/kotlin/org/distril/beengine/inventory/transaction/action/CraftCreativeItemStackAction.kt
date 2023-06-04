package org.distril.beengine.inventory.transaction.action

import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket.ContainerEntry
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.player.Player

class CraftCreativeItemStackAction(transaction: ItemStackTransaction) : ItemStackAction(transaction = transaction) {

    override fun isValid(player: Player): Boolean {
        val creativeOutput = this.transaction.creativeOutput
        if (creativeOutput != null && player.isCreative) {
            creativeOutput.count = creativeOutput.maxCount
            return true
        }

        return false
    }

    override fun execute(player: Player) = true

    override fun getContainers(player: Player): List<ContainerEntry> = listOf()
}
