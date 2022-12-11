package org.distril.beengine.player.handler;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.*;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.network.Network;
import org.distril.beengine.network.data.LoginData;
import org.distril.beengine.player.Player;
import org.distril.beengine.server.Server;

@Log4j2
public class ResourcePackPacketHandler implements BedrockPacketHandler {

	private final Server server;
	private final BedrockServerSession session;
	private final LoginData loginData;


	public ResourcePackPacketHandler(Server server, BedrockServerSession session, LoginData loginData) {
		this.server = server;
		this.session = session;
		this.loginData = loginData;

		// Notify client about all resource packs on the server
		var packet = new ResourcePacksInfoPacket();
		packet.setForcedToAccept(false);
		session.sendPacket(packet);
	}

	@Override
	public boolean handle(ResourcePackClientResponsePacket packet) {
		switch (packet.getStatus()) {
			case REFUSED -> {
				this.session.disconnect("disconnectionScreen.noReason");
				return true;
			}

			case SEND_PACKS -> {
				// todo
				return true;
			}

			case HAVE_ALL_PACKS -> {
				var stackPacket = new ResourcePackStackPacket();
				stackPacket.setForcedToAccept(false);
				stackPacket.setGameVersion(Network.CODEC.getMinecraftVersion());
				this.session.sendPacket(stackPacket);
				return true;
			}

			case COMPLETED -> {
				Player player = new Player(this.server, this.session, this.loginData);
				player.initialize();

				this.session.addDisconnectHandler(reason -> this.server.getScheduler().prepareTask(() -> {
					this.server.removePlayer(player);

					player.onDisconnect();
				}).async().schedule());

				this.session.setPacketHandler(new PlayerPacketHandler());
				return true;
			}
		}

		return true;
	}

	@Override
	public boolean handle(ResourcePackChunkRequestPacket packet) {
		// todo
		return BedrockPacketHandler.super.handle(packet);
	}

	@Override
	public boolean handle(PacketViolationWarningPacket packet) {
		log.debug("Packet violation for " + packet.getPacketType() + ": " + packet.getContext());
		return true;
	}
}
