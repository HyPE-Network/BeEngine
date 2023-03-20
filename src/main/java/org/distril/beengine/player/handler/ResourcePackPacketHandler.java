package org.distril.beengine.player.handler;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.*;
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
		this.loginData = loginData;

		this.session.getHardcodedBlockingId().set(Material.SHIELD.getItemRuntimeId());
		this.session.sendPacket(new ResourcePacksInfoPacket()); // todo: add resource packs
	}

	@Override
	public boolean handle(ResourcePackClientResponsePacket packet) {
		switch (packet.getStatus()) {
			case REFUSED -> this.session.disconnect("disconnectionScreen.noReason");

			case SEND_PACKS -> {
				// todo
			}

			case HAVE_ALL_PACKS -> {
				var stackPacket = new ResourcePackStackPacket();
				stackPacket.setGameVersion(Network.CODEC.getMinecraftVersion());
				this.session.sendPacket(stackPacket);
			}

			case COMPLETED -> {
				this.server.getScheduler().prepareTask(() -> {
					var player = new Player(this.server, this.session, this.loginData);

					this.session.addDisconnectHandler(reason -> {
						this.server.removePlayer(player);

						player.disconnect(reason.name());
					});

					player.initialize();
					player.completePlayerInitialization();

					this.session.setPacketHandler(new PlayerPacketHandler(player));
				}).async(true).schedule();
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
		log.debug("Packet violation: {}", packet);
		return true;
	}
}
