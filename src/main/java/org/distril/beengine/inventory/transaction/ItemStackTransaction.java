package org.distril.beengine.inventory.transaction;

import com.nukkitx.protocol.bedrock.data.inventory.ContainerSlotType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.transaction.action.ItemStackAction;
import org.distril.beengine.player.Player;

import java.util.*;

import static com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket.*;

@Log4j2
@Getter
@RequiredArgsConstructor
public class ItemStackTransaction {

	private final Map<ContainerSlotType, List<ItemEntry>> containers = new EnumMap<>(ContainerSlotType.class);

	private final Player player;

	@Setter
	private ResponseStatus status = ResponseStatus.OK;

	public boolean handle(ItemStackAction action) {
		if (!action.isValid(this.player)) {
			log.warn("Failed validation check on {}", action.getClass().getSimpleName());
			action.onExecuteFail(this.player);

			return false;
		}

		if (action.execute(this.player)) {
			action.onExecuteSuccess(this.player);

			return true;
		}

		action.onExecuteFail(this.player);
		return false;
	}

	public void clear() {
		this.containers.clear();
	}

	public void addContainers(Collection<ContainerEntry> containers) {
		containers.forEach(entry -> {
			var list = this.containers.computeIfAbsent(entry.getContainer(), x -> new ArrayList<>());
			list.addAll(entry.getItems());
		});
	}

	public List<ContainerEntry> getContainerEntries() {
		return this.containers.keySet()
				.stream()
				.map(container -> new ContainerEntry(container, this.containers.get(container)))
				.toList();
	}

	public Inventory getInventoryByType(ContainerSlotType type) {
		return switch (type) {
			case HOTBAR, HOTBAR_AND_INVENTORY, INVENTORY, OFFHAND -> this.player.getInventory();
			case CURSOR -> this.player.getInventory().getCursorInventory();
			default -> this.player.getOpenedInventory();
		};
	}
}
