package org.distril.beengine.player.handler;

import com.nukkitx.protocol.bedrock.data.inventory.ItemStackRequest;
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.PlaceStackRequestActionData;
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.StackRequestActionData;
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.SwapStackRequestActionData;
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.TakeStackRequestActionData;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.*;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.inventory.transaction.action.PlaceItemStackAction;
import org.distril.beengine.inventory.transaction.action.SwapItemStackAction;
import org.distril.beengine.inventory.transaction.action.TakeItemStackAction;
import org.distril.beengine.network.data.transaction.ItemUseTransaction;
import org.distril.beengine.player.Player;

import java.util.Arrays;
import java.util.Collections;

@Log4j2
public class InventoryPacketHandler implements BedrockPacketHandler {

	private final Player player;

	private final ItemStackTransaction transaction;

	public InventoryPacketHandler(Player player) {
		this.player = player;
		this.transaction = new ItemStackTransaction(player);
	}

	@Override
	public boolean handle(ItemStackRequestPacket packet) {
		var responsePacket = new ItemStackResponsePacket();

		boolean continueActions = true;
		for (ItemStackRequest request : packet.getRequests()) {
			for (StackRequestActionData action : request.getActions()) {
				if (!continueActions) {
					break;
				}

				switch (action.getType()) {
					case TAKE -> {
						var source = ((TakeStackRequestActionData) action).getSource();
						var target = ((TakeStackRequestActionData) action).getDestination();

						continueActions = this.transaction.handle(new TakeItemStackAction(
								source,
								target,
								this.transaction,
								((TakeStackRequestActionData) action).getCount()
						));
					}

					case PLACE -> {
						var source = ((PlaceStackRequestActionData) action).getSource();
						var target = ((PlaceStackRequestActionData) action).getDestination();

						continueActions = this.transaction.handle(new PlaceItemStackAction(
								source,
								target,
								this.transaction,
								((PlaceStackRequestActionData) action).getCount()
						));
					}

					case SWAP -> {
						var source = ((SwapStackRequestActionData) action).getSource();
						var target = ((SwapStackRequestActionData) action).getDestination();

						continueActions = this.transaction.handle(new SwapItemStackAction(source, target, this.transaction));
					}

					default -> log.warn("Missing inventory action handler: " + action.getType());
				}
			}

			responsePacket.getEntries().add(new ItemStackResponsePacket.Response(
					this.transaction.getStatus(),
					request.getRequestId(),
					this.transaction.getContainerEntries()
			));

			this.transaction.clear();
		}

		this.player.sendPacket(responsePacket);
		return true;
	}

	@Override
	public boolean handle(InventoryTransactionPacket packet) {
		if (this.player.isSpectator()) {
			return true;
		}

		switch (packet.getTransactionType()) {
			case ITEM_USE -> {
				var transaction = ItemUseTransaction.read(packet);

				switch (transaction.getType()) {
					case CLICK_BLOCK -> {
						// player.setUsingItem(false);

						var blockPosition = transaction.getBlockPosition();
						var world = player.getWorld();
						var blockFace = transaction.getBlockFace();
						if (player.canInteract(blockPosition.toFloat().add(0.5, 0.5, 0.5))) {
							var clientItem = transaction.getItemInHand();
							var serverItem = player.getInventory().getItemInHand();
							if (player.isCreative()) {
								if (world.useItemOn(blockPosition, serverItem, blockFace, packet.getClickPosition(), player) != null) {
									return true;
								}
							} else if (serverItem.equals(clientItem)) {
								var oldServerItem = serverItem;
								if ((serverItem = world.useItemOn(blockPosition, serverItem, blockFace, packet.getClickPosition(), player)) != null) {
									if (!serverItem.equals(oldServerItem) ||
											serverItem.getCount() != oldServerItem.getCount()) {
										player.getInventory().setItemInHand(serverItem);
										player.getInventory().sendHeldItem(player.getViewers());
									}

									return true;
								}
							}
						}

						player.getInventory().sendHeldItem(Collections.singleton(player));

						if (blockPosition.distanceSquared(player.getPosition().toInt()) > 10000) {
							return true;
						}

						var target = world.getBlock(blockPosition);
						var block = target.getSide(blockFace);

						world.sendBlocks(Collections.singleton(player), Arrays.asList(target, block), UpdateBlockPacket.FLAG_ALL_PRIORITY);
						return true;
					}

					case CLICK_AIR -> {

					}

					case BREAK_BLOCK -> {

					}
				}
			}

			case ITEM_USE_ON_ENTITY -> {

			}

			case ITEM_RELEASE -> {

			}

			default -> log.info("Unhandled transaction type: {}", packet.getTransactionType());
		}

		log.info(packet.toString());
		return true;
	}

	@Override
	public boolean handle(ContainerClosePacket packet) {
		this.player.closeOpenedInventory();
		return true;
	}
}
