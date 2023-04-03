package org.distril.beengine.player.handler

import com.nukkitx.protocol.bedrock.data.inventory.TransactionType
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.*
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler
import com.nukkitx.protocol.bedrock.packet.*
import org.apache.logging.log4j.LogManager
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.inventory.transaction.action.*
import org.distril.beengine.material.item.ItemPalette
import org.distril.beengine.network.data.transaction.ItemUseTransaction
import org.distril.beengine.player.Player

class InventoryPacketHandler(private val player: Player) : BedrockPacketHandler {

	private val transaction = ItemStackTransaction(this.player)

	override fun handle(packet: ItemStackRequestPacket): Boolean {
		val responsePacket = ItemStackResponsePacket()
		var continueActions = true
		for (request in packet.requests) {
			for (action in request.actions) {
				if (!continueActions) break

				when (action.type) {
					StackRequestActionType.TAKE -> {
						val takeAction = action as TakeStackRequestActionData
						continueActions = this.transaction.handle(
							TakeItemStackAction(
								takeAction.source, takeAction.destination,
								this.transaction, takeAction.count.toInt(), request.requestId
							)
						)
					}

					StackRequestActionType.PLACE -> {
						val placeAction = action as PlaceStackRequestActionData
						continueActions = this.transaction.handle(
							PlaceItemStackAction(
								placeAction.source, placeAction.destination,
								this.transaction, placeAction.count.toInt(), request.requestId
							)
						)
					}

					StackRequestActionType.SWAP -> {
						val swapAction = action as SwapStackRequestActionData
						continueActions = this.transaction.handle(
							SwapItemStackAction(
								swapAction.source, swapAction.destination, this.transaction
							)
						)
					}

					StackRequestActionType.DROP -> {
						// todo when i added player and world method for drop items
					}

					StackRequestActionType.DESTROY -> {
						val destroyAction = action as DestroyStackRequestActionData
						continueActions = this.transaction.handle(
							DestroyItemStackAction(
								destroyAction.source, destroyAction.count.toInt(), this.transaction
							)
						)
					}

					StackRequestActionType.CRAFT_CREATIVE -> {
						val craftCreativeAction = action as CraftCreativeStackRequestActionData
						this.transaction.creativeOutput =
							ItemPalette.getCreativeItem(craftCreativeAction.creativeItemNetworkId)
						continueActions = this.transaction.handle(CraftCreativeItemStackAction(this.transaction))
					}

					StackRequestActionType.CRAFT_RESULTS_DEPRECATED -> {
						// skip it because deprecated
					}

					else -> log.warn("Missing inventory action handler: ${action.type}")
				}
			}

			responsePacket.entries.add(
				ItemStackResponsePacket.Response(
					this.transaction.status,
					request.requestId,
					this.transaction.containersEntries
				)
			)

			this.transaction.clear()
		}

		this.player.sendPacket(responsePacket)
		return true
	}

	override fun handle(packet: InventoryTransactionPacket): Boolean {
		if (this.player.isSpectator) return true

		when (packet.transactionType) {
			TransactionType.ITEM_USE -> {
				val transaction = ItemUseTransaction.read(packet)
				when (transaction.type) {
					ItemUseTransaction.Type.CLICK_BLOCK -> {
						this.player.setUsingItem(false)

						val blockPosition = transaction.blockPosition
						val world = player.world
						val blockFace = transaction.blockFace
						if (this.player.canInteract(blockPosition.toFloat().add(0.5, 0.5, 0.5))) {
							val clientItem = transaction.itemInHand
							var serverItem = this.player.inventory.getItemInHand()
							if (this.player.isCreative) {
								if (world.useItemOn(blockPosition, serverItem, blockFace, this.player) != null) {
									return true
								}
							} else if (serverItem == clientItem) {
								val oldServerItem = serverItem
								if (world.useItemOn(blockPosition, serverItem, blockFace, this.player).run {
										if (this != null) {
											serverItem = this
											return@run true
										}

										return@run false
									}
								) {
									if (serverItem != oldServerItem || serverItem.count != oldServerItem.count) {
										this.player.inventory.setItemInHand(serverItem)
										this.player.inventory.sendHeldItem(this.player.viewers)
									}

									return true
								}
							}
						}

						this.player.inventory.sendHeldItem(listOf(this.player))
						if (blockPosition.distanceSquared(player.position.toInt()) > 10000) return true

						val target = world.getBlock(blockPosition)
						val block = target.getSide(blockFace)
						world.sendBlocks(setOf(this.player), listOf(target, block))

						this.player.setUsingItem(false)
						return true
					}

					ItemUseTransaction.Type.CLICK_AIR -> TODO()

					ItemUseTransaction.Type.BREAK_BLOCK -> TODO()
				}
			}

			TransactionType.ITEM_USE_ON_ENTITY -> TODO()

			TransactionType.ITEM_RELEASE -> TODO()

			else -> log.info("Unhandled transaction type: ${packet.transactionType}")
		}

		log.info(packet.toString())
		return true
	}

	override fun handle(packet: MobEquipmentPacket): Boolean {
		this.player.inventory.heldItemIndex = packet.hotbarSlot
		return true
	}

	override fun handle(packet: ContainerClosePacket): Boolean {
		this.player.closeOpenedInventory()
		return true
	}

	override fun handle(packet: PacketViolationWarningPacket): Boolean {
		log.warn("Packet violation $packet")
		return true
	}

	companion object {

		private val log = LogManager.getLogger(InventoryPacketHandler::class.java)
	}
}
