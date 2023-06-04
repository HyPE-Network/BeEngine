package org.distril.beengine.network

import com.nukkitx.protocol.bedrock.BedrockPong
import com.nukkitx.protocol.bedrock.BedrockServer
import com.nukkitx.protocol.bedrock.BedrockServerEventHandler
import com.nukkitx.protocol.bedrock.BedrockServerSession
import com.nukkitx.protocol.bedrock.v567.Bedrock_v567patch
import org.distril.beengine.player.handler.LoginPacketHandler
import org.distril.beengine.server.Server
import org.distril.beengine.util.Utils.getLogger
import java.net.InetSocketAddress
import java.util.concurrent.CompletionException

class Network(
    ip: String,
    port: Int
) : BedrockServerEventHandler {

    private val bedrockServer: BedrockServer

    init {
        val bindAddress = InetSocketAddress(ip, port)
        bedrockServer = BedrockServer(bindAddress, Runtime.getRuntime().availableProcessors())
        bedrockServer.handler = this


        PONG.apply {
            motd = Server.settings.motd
            subMotd = motd
            maximumPlayerCount = Server.settings.maximumPlayers
            ipv4Port = port
            ipv6Port = ipv4Port
        }
    }

    fun start() {
        try {
            bedrockServer.bind().join()
            log.info("Server started on ${bedrockServer.bindAddress} with ${CODEC.minecraftVersion} Minecraft version")
        } catch (exception: CompletionException) {
            if (exception.cause is Exception) throw exception.cause as Exception

            throw exception
        }
    }

    override fun onConnectionRequest(address: InetSocketAddress) = true

    override fun onQuery(address: InetSocketAddress) = PONG

    override fun onSessionCreation(session: BedrockServerSession) {
        session.apply {
            compressionLevel = Server.settings.compressionLevel
            isLogging = false
            packetCodec = CODEC
            packetHandler = LoginPacketHandler(session)
        }
    }

    fun stop() = this.bedrockServer.close(true)

    companion object {

        private val log = Network.getLogger()

        val CODEC = Bedrock_v567patch.BEDROCK_V567PATCH!!

        val PONG = BedrockPong().apply {
            edition = "MCPE"
            gameType = "Survival"
            protocolVersion = CODEC.protocolVersion
            version = CODEC.minecraftVersion
            playerCount = 0
        }
    }
}
