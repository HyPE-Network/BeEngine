package org.distril.beengine.player.handler

import com.nukkitx.protocol.bedrock.data.inventory.TransactionType
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.*
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler
import com.nukkitx.protocol.bedrock.packet.*
import org.distril.beengine.inventory.transaction.ItemStackTransaction
import org.distril.beengine.inventory.transaction.action.*
import org.distril.beengine.material.item.ItemPalette
import org.distril.beengine.network.data.transaction.ItemUseTransaction
import org.distril.beengine.player.Player
import org.distril.beengine.util.Utils.getLogger

class InventoryPacketHandler(private val player: Player) : BedrockPacketHandler {

	private val transaction = ItemStackTransaction(this.player)

	override fun handle(packet: ItemStackRequestPacket): Boolean {
		log.debug(packet.toString())

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

					else -> log.warn("Missing inventory action handler: $packet")
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
		log.debug(packet.toString())

		if (this.player.isSpectator || !this.player.isAlive) return true

		when (packet.transactionType) {
			TransactionType.ITEM_USE -> {
				val transaction = ItemUseTransaction.read(packet)
				val blockPosition = transaction.blockPosition
				if (this.player.canInteract(blockPosition.toFloat())) {
					when (transaction.type) {
						ItemUseTransaction.Type.CLICK_BLOCK -> {
							this.player.setUsingItem(false)

							val world = player.world
							val blockFace = transaction.blockFace
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

							this.player.inventory.sendHeldItem(listOf(this.player))

							val target = world.getBlock(blockPosition)
							val block = target.getSide(blockFace)
							world.sendBlocks(setOf(this.player), listOf(target, block))

							this.player.setUsingItem(true)
							return true
						}

						ItemUseTransaction.Type.CLICK_AIR -> {
							// todo
						}

						ItemUseTransaction.Type.BREAK_BLOCK -> {
							// todo
						}
					}
				}
			}

			TransactionType.ITEM_USE_ON_ENTITY -> TODO("ITEM_USE_ON_ENTITY")

			TransactionType.ITEM_RELEASE -> TODO("ITEM_RELEASE")

			else -> log.info("Unhandled transaction type: $packet")
		}

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
		log.debug("Packet violation {}", packet)
		return true
	}

	companion object {

		private val log = InventoryPacketHandler.getLogger()
	}
}
