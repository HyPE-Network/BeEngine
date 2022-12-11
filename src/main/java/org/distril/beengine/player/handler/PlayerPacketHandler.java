package org.distril.beengine.player.handler;

import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.PacketViolationWarningPacket;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PlayerPacketHandler implements BedrockPacketHandler {

	@Override
	public boolean handle(PacketViolationWarningPacket packet) {
		log.debug("Packet violation for " + packet.getPacketType() + ": " + packet.getContext());
		return true;
	}
}
