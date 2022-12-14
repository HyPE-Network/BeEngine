package org.distril.beengine.player.handler;

import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.ContainerClosePacket;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;
import com.nukkitx.protocol.bedrock.packet.ItemStackRequestPacket;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.player.Player;

@Log4j2
@AllArgsConstructor
public class InventoryPacketHandler implements BedrockPacketHandler {

	private final Player player;

	@Override
	public boolean handle(ItemStackRequestPacket packet) {
		log.info(packet.toString());
		return true;
	}

	@Override
	public boolean handle(InventoryTransactionPacket packet) {
		log.info(packet.toString());
		return true;
	}

	@Override
	public boolean handle(ContainerClosePacket packet) {
		this.player.closeOpenedInventory();
		return true;
	}
}
