package org.distril.beengine.player.handler

import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler
import com.nukkitx.protocol.bedrock.packet.*
import org.apache.logging.log4j.LogManager
import org.distril.beengine.player.Player
import org.distril.beengine.server.Server

class PlayerPacketHandler(private val player: Player) : BedrockPacketHandler {

	private val inventoryPacketHandler = InventoryPacketHandler(player)

	override fun handle(packet: CommandRequestPacket): Boolean {
		Server.dispatchCommand(this.player, packet.command.substring(1))
		return true
	}

	override fun handle(packet: InteractPacket): Boolean {
		if (packet.action == InteractPacket.Action.OPEN_INVENTORY) {
			this.player.openInventory(this.player.inventory)
		}

		return true
	}

	override fun handle(packet: MovePlayerPacket): Boolean {
		if (!this.player.isSpawned) return true

		val to = packet.position.sub(0f, this.player.eyeHeight, 0f)
		val from = this.player.position

		val pitch = packet.rotation.x % 360
		var yaw = packet.rotation.y % 360
		if (yaw < 0) yaw += 360f

		val distance = to.distanceSquared(from)
		if (distance == 0f && pitch == this.player.pitch && yaw == this.player.yaw) {
			return true
		}

		this.player.setRotation(pitch, yaw)
		this.player.position = to

		// player.sendPosition(MovePlayerPacket.Mode.NORMAL);
		return true
	}

	override fun handle(packet: RequestChunkRadiusPacket): Boolean {
		this.player.chunkManager.radius = packet.radius
		return true
	}

	override fun handle(packet: ItemStackRequestPacket) = this.inventoryPacketHandler.handle(packet)

	override fun handle(packet: InventoryTransactionPacket) = this.inventoryPacketHandler.handle(packet)

	override fun handle(packet: MobEquipmentPacket) = this.inventoryPacketHandler.handle(packet)

	override fun handle(packet: ContainerClosePacket) = this.inventoryPacketHandler.handle(packet)

	override fun handle(packet: PacketViolationWarningPacket): Boolean {
		log.warn("Packet violation $packet")
		return true
	}

	companion object {

		private val log = LogManager.getLogger(PlayerPacketHandler::class.java)
	}
}
