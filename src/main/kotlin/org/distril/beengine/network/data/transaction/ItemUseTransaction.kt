package org.distril.beengine.network.data.transaction

import com.nukkitx.math.vector.Vector3f
import com.nukkitx.math.vector.Vector3i
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket
import org.distril.beengine.material.item.Item
import org.distril.beengine.util.Direction
import org.distril.beengine.util.ItemUtils

class ItemUseTransaction private constructor(
	val type: Type,
	val blockPosition: Vector3i,
	val blockFace: Direction,
	val hotbarSlot: Int,
	val itemInHand: Item,
	val playerPosition: Vector3f,
	val clickPosition: Vector3f,
	val blockRuntimeId: Int
) {

	enum class Type {

		CLICK_BLOCK,
		CLICK_AIR,
		BREAK_BLOCK;

		companion object {

			fun fromTypeId(typeId: Int) = Type.values()[typeId]
		}
	}

	companion object {

		fun read(packet: InventoryTransactionPacket): ItemUseTransaction {
			val type = Type.fromTypeId(packet.actionType)
			val blockPosition = packet.blockPosition
			val blockFace = Direction.fromIndex(packet.blockFace)!!
			val hotbarSlot = packet.hotbarSlot
			val itemInHand = ItemUtils.fromNetwork(packet.itemInHand)
			val playerPosition = packet.playerPosition
			val clickPosition = packet.clickPosition
			val blockRuntimeId = packet.blockRuntimeId
			return ItemUseTransaction(
				type, blockPosition, blockFace, hotbarSlot, itemInHand, playerPosition,
				clickPosition, blockRuntimeId
			)
		}
	}
}
