package org.distril.beengine.inventory

import com.nukkitx.protocol.bedrock.data.inventory.ContainerType

enum class InventoryType(
	val size: Int = 0,
	val title: String,
	val containerType: ContainerType
) {

	PLAYER(25 + 9, "Player", ContainerType.INVENTORY),
	CURSOR(1, "Cursor", ContainerType.INVENTORY),
	CRAFTING(4, "Crafting", ContainerType.WORKBENCH)
}
