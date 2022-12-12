package org.distril.beengine.inventory;

import com.nukkitx.protocol.bedrock.data.inventory.ContainerType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InventoryType {

	PLAYER(36, "Player", ContainerType.INVENTORY),
	CURSOR(1, "Cursor", ContainerType.INVENTORY);

	private final int size;
	private final String title;
	private final ContainerType containerType;
}
