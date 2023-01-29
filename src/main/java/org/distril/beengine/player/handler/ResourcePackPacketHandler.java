package org.distril.beengine.player.handler;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.PacketViolationWarningPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackStackPacket;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.material.Material;
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
		this.session.getHardcodedBlockingId().set(Material.SHIELD.getItemRuntimeId());
		this.loginData = loginData;
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
				var resourcePackStackPacket = new ResourcePackStackPacket();
				resourcePackStackPacket.setGameVersion(Network.CODEC.getMinecraftVersion());
				this.session.sendPacket(resourcePackStackPacket);
				return true;
			}

			case COMPLETED -> {
				var player = new Player(this.server, this.session, this.loginData);
				player.initialize();

				this.session.addDisconnectHandler(reason -> this.server.getScheduler().prepareTask(() -> {
					this.server.removePlayer(player);

					player.onDisconnect();
				}).async().schedule());

				this.session.setPacketHandler(new PlayerPacketHandler(player));
				return true;
			}
		}

		return true;
	}

	@Override
	public boolean handle(PacketViolationWarningPacket packet) {
		log.debug("Packet violation: {}", packet);
		return true;
	}
}
