package org.distril.beengine.player.handler;

import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.InteractPacket;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;
import com.nukkitx.protocol.bedrock.packet.ItemStackRequestPacket;
import com.nukkitx.protocol.bedrock.packet.PacketViolationWarningPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.player.Player;

@Log4j2
@RequiredArgsConstructor
public class PlayerPacketHandler implements BedrockPacketHandler {

	private final Player player;

	@Override
	public boolean handle(InteractPacket packet) {
		if (packet.getAction() == InteractPacket.Action.OPEN_INVENTORY) {
			this.player.openInventory(this.player.getInventory());
		}

		log.info(packet.toString());
		return true;
	}

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
	public boolean handle(PacketViolationWarningPacket packet) {
		log.debug("Packet violation for " + packet.getPacketType() + ": " + packet.getContext());
		return true;
	}
}
