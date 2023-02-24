package org.distril.beengine.inventory;

import com.nukkitx.protocol.bedrock.data.inventory.ContainerType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryType {

	PLAYER(36, "Player", ContainerType.INVENTORY),
	CURSOR(1, "Cursor", ContainerType.INVENTORY),
	CRAFTING(4, "Crafting", ContainerType.WORKBENCH);

	private final int size;
	private final String title;
	private final ContainerType containerType;
}
