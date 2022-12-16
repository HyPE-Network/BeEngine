package org.distril.beengine.player.handler;

import com.nukkitx.protocol.bedrock.data.inventory.ItemStackRequest;
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.PlaceStackRequestActionData;
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.StackRequestActionData;
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.SwapStackRequestActionData;
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.TakeStackRequestActionData;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.ContainerClosePacket;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;
import com.nukkitx.protocol.bedrock.packet.ItemStackRequestPacket;
import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.inventory.transaction.action.MoveItemStackAction;
import org.distril.beengine.inventory.transaction.action.SwapItemStackAction;
import org.distril.beengine.player.Player;

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

						continueActions = this.transaction.handle(new MoveItemStackAction(
								source,
								target,
								((TakeStackRequestActionData) action).getCount()
						));
					}

					case PLACE -> {
						var source = ((PlaceStackRequestActionData) action).getSource();
						var target = ((PlaceStackRequestActionData) action).getDestination();

						continueActions = this.transaction.handle(new MoveItemStackAction(
								source,
								target,
								((PlaceStackRequestActionData) action).getCount()
						));
					}

					case SWAP -> {
						var source = ((SwapStackRequestActionData) action).getSource();
						var target = ((SwapStackRequestActionData) action).getDestination();

						continueActions = this.transaction.handle(new SwapItemStackAction(source, target));
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
		log.info(packet.toString());
		return true;
	}

	@Override
	public boolean handle(ContainerClosePacket packet) {
		this.player.closeOpenedInventory();
		return true;
	}
}
