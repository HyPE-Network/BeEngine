package org.distril.beengine.inventory.defaults;

import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.packet.ContainerOpenPacket;
import lombok.Getter;
import org.distril.beengine.inventory.InventoryType;
import org.distril.beengine.player.Player;

@Getter
public class PlayerInventory extends EntityInventory {

	private final PlayerCursorInventory cursorInventory;

	private int selectedSlot;

	public PlayerInventory(Player player) {
		super(player, InventoryType.PLAYER);

		this.cursorInventory = new PlayerCursorInventory(player);
	}

	@Override
	public void clear() {
		super.clear();

		this.cursorInventory.clear();
	}

	@Override
	protected void onOpen(Player player) {
		var packet = new ContainerOpenPacket();
		packet.setId(this.getId());
		packet.setType(this.getType().getContainerType());
		packet.setBlockPosition(Vector3i.ZERO);
		packet.setUniqueEntityId(player.getId());

		player.sendPacket(packet);

		this.sendSlots(player, this.getItems());
	}
}
