package org.distril.beengine.player.handler

import com.nukkitx.protocol.bedrock.BedrockServerSession
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler
import com.nukkitx.protocol.bedrock.packet.PacketViolationWarningPacket
import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket
import com.nukkitx.protocol.bedrock.packet.ResourcePackStackPacket
import com.nukkitx.protocol.bedrock.packet.ResourcePacksInfoPacket
import org.distril.beengine.material.Material
import org.distril.beengine.network.Network
import org.distril.beengine.network.data.LoginData
import org.distril.beengine.player.Player
import org.distril.beengine.server.Server
import org.distril.beengine.util.Utils.getLogger

class ResourcePackPacketHandler(
	private val session: BedrockServerSession,
	private val loginData: LoginData
) : BedrockPacketHandler {

	init {
		this.session.hardcodedBlockingId.set(Material.SHIELD.itemRuntimeId)
		this.session.sendPacket(ResourcePacksInfoPacket()) // todo: add resource packs
	}

	override fun handle(packet: ResourcePackClientResponsePacket): Boolean {
		when (packet.status) {
			ResourcePackClientResponsePacket.Status.SEND_PACKS -> TODO()

			ResourcePackClientResponsePacket.Status.HAVE_ALL_PACKS -> {
				val stackPacket = ResourcePackStackPacket()
				stackPacket.gameVersion = Network.CODEC.minecraftVersion

				this.session.sendPacket(stackPacket)
			}

			ResourcePackClientResponsePacket.Status.COMPLETED -> {
				Server.scheduler.scheduleTask(async = true) {
					val player = Player(this.session, this.loginData)
					this.session.apply {
						this.addDisconnectHandler {
							Server.removePlayer(player)
							player.disconnect(it.name)
						}

						this.packetHandler = PlayerPacketHandler(player)
					}

					player.initialize()
				}
			}

			else -> this.session.disconnect("disconnectionScreen.noReason")
		}

		return true
	}

	override fun handle(packet: PacketViolationWarningPacket): Boolean {
		log.debug("Packet violation: {}", packet)
		return true
	}

	companion object {

		private val log = ResourcePackPacketHandler.getLogger()
	}
}
