package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket;
import lombok.Getter;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.material.item.ItemPalette;
import org.distril.beengine.player.Player;

import java.util.Collections;
import java.util.List;

@Getter
public class CraftCreativeItemStackAction extends ItemStackAction {

	private final Item creativeItem;


	public CraftCreativeItemStackAction(int creativeItemNetworkId, ItemStackTransaction transaction) {
		super(null, null, transaction);
		this.creativeItem = ItemPalette.getCreativeItem(creativeItemNetworkId);
	}

	@Override
	public boolean isValid(Player player) {
		if (this.creativeItem != null && player.isCreative()) {
			player.getInventory().getCraftingInventory().setCreativeOutput(this.creativeItem);
			return true;
		}

		return false;
	}

	@Override
	public boolean execute(Player player) {
		return true;
	}

	@Override
	protected List<ItemStackResponsePacket.ContainerEntry> getContainers(Player player) {
		return Collections.emptyList();
	}
}
