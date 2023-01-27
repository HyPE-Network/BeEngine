package org.distril.beengine.network.data.transaction;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.util.Direction;
import org.distril.beengine.util.ItemUtil;

@Getter
@RequiredArgsConstructor
public class ItemUseTransaction {

	private final ItemUseType type;

	private final Vector3i blockPosition;
	private final Direction blockFace;

	private final int hotbarSlot;
	private final Item itemInHand;

	private final Vector3f playerPosition, clickPosition;

	private final int blockRuntimeId;

	public static ItemUseTransaction read(InventoryTransactionPacket packet) {
		var type = ItemUseType.fromTypeId(packet.getActionType());

		var blockPosition = packet.getBlockPosition();
		var blockFace = Direction.fromIndex(packet.getBlockFace());

		var hotbarSlot = packet.getHotbarSlot();
		var itemInHand = ItemUtil.fromNetwork(packet.getItemInHand());

		var playerPosition = packet.getPlayerPosition();
		var clickPosition = packet.getClickPosition();

		var blockRuntimeId = packet.getBlockRuntimeId();
		return new ItemUseTransaction(type, blockPosition, blockFace, hotbarSlot, itemInHand, playerPosition,
				clickPosition, blockRuntimeId);
	}

	public enum ItemUseType {

		CLICK_BLOCK,
		CLICK_AIR,
		BREAK_BLOCK;

		public static ItemUseType fromTypeId(int typeId) {
			return ItemUseType.values()[typeId];
		}
	}
}
