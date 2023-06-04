package org.distril.beengine.inventory.transaction.action

import com.nukkitx.protocol.bedrock.data.inventory.StackRequestSlotInfoData
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket.ContainerEntry
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.player.Player

class SwapItemStackAction(
    from: StackRequestSlotInfoData,
    to: StackRequestSlotInfoData,
    transaction: ItemStackTransaction
) : ItemStackAction(from, to, transaction) {

    override fun isValid(player: Player) = this.fromItem?.equal(this.toItem!!) == false

    override fun execute(player: Player): Boolean {
        this.fromItem = this.toItem
        this.toItem = this.fromItem
        return true
    }

    override fun getContainers(player: Player): List<ContainerEntry> {
        return listOf(
            ContainerEntry(
                this.from?.container,
                listOf(this.from?.toNetwork())
            ),
            ContainerEntry(
                this.to?.container,
                listOf(this.to?.toNetwork())
            )
        )
    }
}
