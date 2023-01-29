package org.distril.beengine.inventory.impl;

import com.nukkitx.protocol.bedrock.packet.ContainerOpenPacket;
import com.nukkitx.protocol.bedrock.packet.MobArmorEquipmentPacket;
import com.nukkitx.protocol.bedrock.packet.MobEquipmentPacket;
import lombok.Getter;
import org.distril.beengine.entity.EntityCreature;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.InventoryHolder;
import org.distril.beengine.inventory.InventoryType;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.ItemUtils;

import java.util.Collection;

@Getter
public class CreatureInventory extends Inventory {

	private Item helmet = Item.AIR;
	private Item chestplate = Item.AIR;
	private Item leggings = Item.AIR;
	private Item boots = Item.AIR;

	private int heldItemIndex;

	public CreatureInventory(InventoryHolder holder) {
		this(holder, Integer.MIN_VALUE);
	}

	public CreatureInventory(InventoryHolder holder, int overrideId) {
		super(holder, InventoryType.PLAYER, overrideId);
	}

	@Override
	public void clear() {
		super.clear();

		this.setHelmet(null);
		this.setChestplate(null);
		this.setLeggings(null);
		this.setBoots(null);

		this.setItemInHand(null);
	}

	public Item getItemInHand() {
		return this.getItem(this.getHeldItemIndex());
	}

	public void setItemInHand(Item item) {
		this.setItem(this.getHeldItemIndex(), item);
	}

	public void setHeldItemIndex(int heldItemIndex) {
		if (heldItemIndex >= 0 && heldItemIndex <= this.getType().getSize()) {
			this.heldItemIndex = heldItemIndex;
			this.sendHeldItem(this.getHolder().getViewers());
		}
	}

	public void sendHeldItem(Collection<Player> players) {
		var itemInHand = this.getItemInHand();

		var packet = new MobEquipmentPacket();
		packet.setItem(ItemUtils.toNetwork(itemInHand));
		packet.setInventorySlot(this.getHeldItemIndex());
		packet.setHotbarSlot(this.getHeldItemIndex());

		players.forEach(player -> {
			packet.setRuntimeEntityId(this.getHolder().getId());

			if (player.equals(this.getHolder())) {
				packet.setRuntimeEntityId(player.getId());
				this.sendSlot(this.getHeldItemIndex(), player);
			}

			player.sendPacket(packet);
		});
	}

	@Override
	protected void onOpen(Player player) {
		ContainerOpenPacket packet = new ContainerOpenPacket();
		packet.setId((byte) this.getId());
		packet.setType(this.getType().getContainerType());

		var holder = this.getHolder();
		packet.setBlockPosition(holder.getPosition().toInt());
		packet.setUniqueEntityId(holder.getId());

		player.sendPacket(packet);
	}

	public void setHelmet(Item helmet) {
		this.helmet = ItemUtils.getAirIfNull(helmet);
		this.sendArmor();
	}

	public void setChestplate(Item chestplate) {
		this.chestplate = ItemUtils.getAirIfNull(chestplate);
		this.sendArmor();
	}

	public void setLeggings(Item leggings) {
		this.leggings = ItemUtils.getAirIfNull(leggings);
		this.sendArmor();
	}

	public void setBoots(Item boots) {
		this.boots = ItemUtils.getAirIfNull(boots);
		this.sendArmor();
	}

	@Override
	public void sendSlots(Player player) {
		super.sendSlots(player);

		this.sendArmor();
	}

	protected void sendArmor() {
		var holder = this.getHolder();

		var packet = new MobArmorEquipmentPacket();
		packet.setRuntimeEntityId(holder.getId());
		packet.setHelmet(ItemUtils.toNetwork(this.helmet));
		packet.setChestplate(ItemUtils.toNetwork(this.chestplate));
		packet.setLeggings(ItemUtils.toNetwork(this.leggings));
		packet.setBoots(ItemUtils.toNetwork(this.boots));

		holder.getViewers().forEach(viewer -> viewer.sendPacket(packet));
	}

	@Override
	public EntityCreature getHolder() {
		return (EntityCreature) super.getHolder();
	}
}
