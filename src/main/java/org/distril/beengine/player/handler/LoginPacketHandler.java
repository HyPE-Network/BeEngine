package org.distril.beengine.player.handler;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.PacketCompressionAlgorithm;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.*;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.network.Network;
import org.distril.beengine.network.data.LoginData;
import org.distril.beengine.server.Server;

@Log4j2
@RequiredArgsConstructor
public class LoginPacketHandler implements BedrockPacketHandler {

	private final BedrockServerSession session;

	private final Server server;

	private LoginData loginData;

	@Override
	public boolean handle(RequestNetworkSettingsPacket packet) {
		int protocolVersion = packet.getProtocolVersion();
		if (protocolVersion != Network.CODEC.getProtocolVersion()) {
			PlayStatusPacket loginFailPacket = new PlayStatusPacket();
			if (protocolVersion > Network.CODEC.getProtocolVersion()) {
				loginFailPacket.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD);
			} else {
				loginFailPacket.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
			}

			this.session.sendPacketImmediately(loginFailPacket);
			return true;
		}

		this.session.setPacketCodec(Network.CODEC);

		var networkSettingsPacket = new NetworkSettingsPacket();
		networkSettingsPacket.setCompressionThreshold(0);
		networkSettingsPacket.setCompressionAlgorithm(PacketCompressionAlgorithm.ZLIB);

		this.session.setCompression(PacketCompressionAlgorithm.ZLIB);
		this.session.sendPacketImmediately(networkSettingsPacket);
		return true;
	}

	@Override
	public boolean handle(LoginPacket packet) {
		this.loginData = LoginData.extract(packet.getChainData(), packet.getSkinData());

		if (this.loginData == null) {
			this.session.disconnect();
			return true;
		}

		try {
			var clientKey = EncryptionUtils.generateKey(this.loginData.getIdentityPublicKey());

			var encryptionKeyPair = EncryptionUtils.createKeyPair();
			var encryptionToken = EncryptionUtils.generateRandomToken();
			var encryptionSecretKey = EncryptionUtils.getSecretKey(encryptionKeyPair.getPrivate(), clientKey,
					encryptionToken);

			var encryptionJWT = EncryptionUtils.createHandshakeJwt(encryptionKeyPair, encryptionToken);

			this.session.enableEncryption(encryptionSecretKey);

			var handshakePacket = new ServerToClientHandshakePacket();
			handshakePacket.setJwt(encryptionJWT.serialize());
			this.session.sendPacket(handshakePacket);
		} catch (Exception exception) {
			log.debug("Failed to initialize packet encryption.", exception);
			this.session.disconnect();
			return true;
		}

		return true;
	}

	@Override
	public boolean handle(ClientToServerHandshakePacket packet) {
		if (this.session.isEncrypted()) {
			this.completeLogin();
		}

		return true;
	}

	@Override
	public boolean handle(PacketViolationWarningPacket packet) {
		log.debug("Packet violation for " + packet.getPacketType() + ": " + packet.getContext());
		return true;
	}

	private void completeLogin() {
		if (this.server.getPlayers().size() >= this.server.getSettings().getMaximumPlayers()) {
			var packet = new PlayStatusPacket();
			packet.setStatus(PlayStatusPacket.Status.FAILED_SERVER_FULL_SUB_CLIENT);
			this.session.sendPacket(packet);
		} else {
			var packet = new PlayStatusPacket();
			packet.setStatus(PlayStatusPacket.Status.LOGIN_SUCCESS);
			this.session.sendPacket(packet);

			this.session.sendPacket(new ResourcePacksInfoPacket()); // todo: add resource packs

			this.session.setPacketHandler(new ResourcePackPacketHandler(this.server, this.session, this.loginData));
		}
	}
}
