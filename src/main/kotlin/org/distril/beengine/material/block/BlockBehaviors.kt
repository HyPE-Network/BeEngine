package org.distril.beengine.material.block

import com.nukkitx.math.vector.Vector3f
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player
import org.distril.beengine.util.Direction

interface BlockBehaviors {

    fun onPlace(item: Item, target: Block, blockFace: Direction, clickPosition: Vector3f, player: Player) = true

    val canBeReplaced get() = false
}
