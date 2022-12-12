package org.distril.beengine.inventory;

import com.nukkitx.protocol.bedrock.data.inventory.ContainerId;
import com.nukkitx.protocol.bedrock.packet.ContainerClosePacket;
import com.nukkitx.protocol.bedrock.packet.InventoryContentPacket;
import com.nukkitx.protocol.bedrock.packet.InventorySlotPacket;
import lombok.Getter;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.ItemUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public abstract class Inventory {

	private static final AtomicInteger ID = new AtomicInteger(0);

	private final Set<Player> viewers = new HashSet<>();

	private final byte id = (byte) ID.incrementAndGet();

	private final InventoryHolder holder;
	private final InventoryType type;

	private final Item[] items;

	public Inventory(InventoryHolder holder, InventoryType type) {
		this.holder = holder;
		this.type = type;
		this.items = new Item[type.getSize()];

		Arrays.fill(this.items, Item.AIR);
	}

	public void setItem(int slot, Item item) {
		if (slot < 0 || slot >= this.items.length) {
			return;
		}

		this.items[slot] = ItemUtils.getAirIfNull(item);
		this.onSlotChange(slot);
	}

	public Item getItem(int slot) {
		if (slot < 0 || slot >= this.items.length) {
			return Item.AIR;
		}

		return this.items[slot];
	}

	public void addItem(Item item) {
		for (int slot = 0; slot < this.items.length; slot++) {
			if (this.items[slot].getMaterial() == Material.AIR) {
				this.items[slot] = ItemUtils.getAirIfNull(item);
				this.onSlotChange(slot);

				return;
			}
		}
	}

	public void clear() {
		Arrays.fill(this.items, Item.AIR);
	}

	public void openFor(Player player) {
		if (this.viewers.add(player)) {
			this.onOpen(player);
		}
	}

	protected void onOpen(Player player) {/**/}

	public void closeFor(Player player) {
		if (this.viewers.remove(player)) {
			this.onClose(player);
		}
	}

	protected void onClose(Player player) {
		ContainerClosePacket packet = new ContainerClosePacket();
		packet.setId(this.id);
		player.sendPacket(packet);
	}

	public void onSlotChange(int slot) {
		this.sendSlot(slot, this.viewers.toArray(new Player[0]));
	}

	protected void sendSlot(int slot, Player... players) {
		var packet = new InventorySlotPacket();
		packet.setContainerId(this.id);
		packet.setSlot(slot);
		packet.setItem(ItemUtils.toNetwork(this.getItem(slot)));

		Arrays.stream(players).forEach(player -> player.sendPacket(packet));
	}

	protected void sendSlots(Player player, Item... items) {
		var packet = new InventoryContentPacket();
		packet.setContainerId(ContainerId.INVENTORY);
		packet.setContents(Arrays.stream(items).map(ItemUtils::toNetwork).collect(Collectors.toList()));
		player.sendPacket(packet);
	}

	public Set<Player> getViewers() {
		return Collections.unmodifiableSet(this.viewers);
	}
}
