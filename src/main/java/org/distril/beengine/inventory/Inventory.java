package org.distril.beengine.inventory;

import com.nukkitx.protocol.bedrock.packet.ContainerClosePacket;
import com.nukkitx.protocol.bedrock.packet.InventoryContentPacket;
import com.nukkitx.protocol.bedrock.packet.InventorySlotPacket;
import lombok.Getter;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.ItemUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public abstract class Inventory {

	private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

	private final Set<Player> viewers = new HashSet<>();

	private final InventoryHolder holder;
	private final InventoryType type;

	private final Item[] items;

	private final int id;

	public Inventory(InventoryHolder holder, InventoryType type) {
		this(holder, type, null);
	}

	public Inventory(InventoryHolder holder, InventoryType type, Integer overrideId) {
		this.holder = holder;
		this.type = type;
		this.items = new Item[type.getSize()];

		Arrays.fill(this.items, Item.AIR);

		if (overrideId != null) {
			this.id = overrideId;
		} else {
			this.id = NEXT_ID.incrementAndGet();
		}
	}

	protected int getId() {
		return this.id;
	}

	public boolean setItem(int slot, Item item) {
		return this.setItem(slot, item, true);
	}

	public boolean setItem(int slot, Item item, boolean send) {
		if (slot < 0 || slot >= this.items.length) {
			return false;
		}

		this.items[slot] = ItemUtils.getAirIfNull(item);

		if (send) {
			this.onSlotChange(slot);
		}

		return true;
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
				this.setItem(slot, item);

				return;
			}
		}
	}

	public void clear() {
		this.clear(true);
	}

	public void clear(boolean send) {
		Arrays.fill(this.items, Item.AIR);

		if (send) {
			this.sendSlots();
		}
	}

	public boolean openFor(Player player) {
		if (this.viewers.add(player)) {
			this.onOpen(player);

			return true;
		}

		return false;
	}

	protected void onOpen(Player player) {/**/}

	public boolean closeFor(Player player) {
		if (this.viewers.remove(player)) {
			this.onClose(player);

			return true;
		}

		return false;
	}

	protected void onClose(Player player) {
		ContainerClosePacket packet = new ContainerClosePacket();
		packet.setId((byte) this.getId());
		player.sendPacket(packet);
	}

	public void onSlotChange(int slot) {
		this.sendSlot(slot, this.viewers.toArray(new Player[0]));
	}

	protected void sendSlot(int slot, Player... players) {
		var packet = new InventorySlotPacket();
		packet.setContainerId(this.getId());
		packet.setSlot(slot);
		packet.setItem(ItemUtils.toNetwork(this.getItem(slot)));

		Arrays.stream(players).forEach(player -> player.sendPacket(packet));
	}

	public void sendSlots(Player player) {
		var packet = new InventoryContentPacket();
		packet.setContainerId(this.getId());
		packet.setContents(Arrays.stream(this.items).map(ItemUtils::toNetwork).collect(Collectors.toList()));
		player.sendPacket(packet);
	}

	public void sendSlots() {
		var packet = new InventoryContentPacket();
		packet.setContainerId(this.getId());
		packet.setContents(Arrays.stream(this.items).map(ItemUtils::toNetwork).collect(Collectors.toList()));

		this.viewers.forEach(viewer -> viewer.sendPacket(packet));
	}

	public Set<Player> getViewers() {
		return new HashSet<>(this.viewers);
	}
}
