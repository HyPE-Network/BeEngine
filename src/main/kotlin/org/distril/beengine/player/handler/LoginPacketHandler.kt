package org.distril.beengine.player.handler

import com.nukkitx.protocol.bedrock.BedrockServerSession
import com.nukkitx.protocol.bedrock.data.PacketCompressionAlgorithm
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler
import com.nukkitx.protocol.bedrock.packet.*
import com.nukkitx.protocol.bedrock.util.EncryptionUtils
import org.distril.beengine.network.Network
import org.distril.beengine.network.data.LoginData
import org.distril.beengine.server.Server
import org.distril.beengine.util.Utils.getLogger
import java.util.regex.Pattern

class LoginPacketHandler(private val session: BedrockServerSession) : BedrockPacketHandler {

	private lateinit var loginData: LoginData

	override fun handle(packet: RequestNetworkSettingsPacket): Boolean {
		val protocolVersion = packet.protocolVersion
		if (protocolVersion != Network.CODEC.protocolVersion) {
			val loginFailedPacket = PlayStatusPacket()
			if (protocolVersion > Network.CODEC.protocolVersion) {
				loginFailedPacket.status = PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD
			} else {
				loginFailedPacket.status = PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD
			}

			this.session.sendPacketImmediately(loginFailedPacket)
			return true
		}

		val networkSettingsPacket = NetworkSettingsPacket()
		networkSettingsPacket.compressionThreshold = 0
		networkSettingsPacket.compressionAlgorithm = PacketCompressionAlgorithm.ZLIB

		this.session.setCompression(networkSettingsPacket.compressionAlgorithm)

		this.session.sendPacketImmediately(networkSettingsPacket)
		return true
	}

	override fun handle(packet: LoginPacket): Boolean {
		try {
			this.loginData = LoginData.extract(packet.chainData, packet.skinData)!!
		} catch (_: NullPointerException) {
			this.session.disconnect()
			return true
		}

		if (!this.loginData.authenticated && Server.settings.isXboxRequired) {
			this.session.disconnect("disconnectionScreen.notAuthenticated")
			return true
		}

		val username = this.loginData.username
		if (!NAME_PATTERN.matcher(username).matches()
			|| username.equals("rcon", true)
			|| username.equals("console", true)
		) {
			this.session.disconnect("disconnectionScreen.invalidName")
			return true
		}

		if (!this.loginData.skin.isValid) {
			this.session.disconnect("disconnectionScreen.invalidSkin")
			return true
		}

		if (!Server.settings.isEncryptionEnabled) {
			this.completeLogin()
			return true
		}

		if (!EncryptionUtils.canUseEncryption()) {
			log.error("Packet encryption is not supported on this machine.")
			this.session.disconnect()
			return true
		}
		try {
			val clientKey = EncryptionUtils.generateKey(this.loginData.identityPublicKey)

			val encryptionKeyPair = EncryptionUtils.createKeyPair()
			val encryptionToken = EncryptionUtils.generateRandomToken()
			val encryptionSecretKey = EncryptionUtils.getSecretKey(
				encryptionKeyPair.private, clientKey,
				encryptionToken
			)

			val encryptionJWT = EncryptionUtils.createHandshakeJwt(encryptionKeyPair, encryptionToken)

			session.enableEncryption(encryptionSecretKey)

			val handshakePacket = ServerToClientHandshakePacket()
			handshakePacket.jwt = encryptionJWT.serialize()

			this.session.sendPacket(handshakePacket)
		} catch (exception: Exception) {
			log.debug("Failed to initialize packet encryption.", exception)
			this.session.disconnect()
			return true
		}

		return true
	}

	override fun handle(packet: ClientToServerHandshakePacket): Boolean {
		if (this.session.isEncrypted) this.completeLogin()

		return true
	}

	override fun handle(packet: PacketViolationWarningPacket): Boolean {
		log.debug("Packet violation {}", packet)
		return true
	}

	private fun completeLogin() {
		if (Server.players.size >= Server.settings.maximumPlayers) {
			val packet = PlayStatusPacket()
			packet.status = PlayStatusPacket.Status.FAILED_SERVER_FULL_SUB_CLIENT
			session.sendPacket(packet)
		} else {
			val packet = PlayStatusPacket()
			packet.status = PlayStatusPacket.Status.LOGIN_SUCCESS

			this.session.sendPacket(packet)

			this.session.packetHandler = ResourcePackPacketHandler(this.session, this.loginData)
		}
	}

	companion object {

		private val log = LoginPacketHandler.getLogger()

		private val NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$")
	}
}
