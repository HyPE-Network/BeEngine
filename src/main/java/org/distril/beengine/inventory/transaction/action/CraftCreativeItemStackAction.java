package org.distril.beengine.inventory.transaction.action;

import com.nukkitx.protocol.bedrock.packet.ItemStackResponsePacket;
import org.distril.beengine.inventory.transaction.ItemStackTransaction;
import org.distril.beengine.player.Player;

import java.util.Collections;
import java.util.List;

public class CraftCreativeItemStackAction extends ItemStackAction {

	public CraftCreativeItemStackAction(ItemStackTransaction transaction) {
		super(null, null, transaction);
	}

	@Override
	public boolean isValid(Player player) {
		return this.getTransaction().getCreativeOutput() != null && player.isCreative();
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
