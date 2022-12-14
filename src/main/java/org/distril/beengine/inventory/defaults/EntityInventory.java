package org.distril.beengine.inventory.defaults;

import com.nukkitx.protocol.bedrock.packet.ContainerOpenPacket;
import com.nukkitx.protocol.bedrock.packet.MobArmorEquipmentPacket;
import lombok.Getter;
import org.distril.beengine.entity.Entity;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.InventoryHolder;
import org.distril.beengine.inventory.InventoryType;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.ItemUtils;

@Getter
public class EntityInventory extends Inventory {

	private Item helmet = Item.AIR;
	private Item chestplate = Item.AIR;
	private Item leggings = Item.AIR;
	private Item boots = Item.AIR;

	public EntityInventory(InventoryHolder holder, InventoryType type) {
		super(holder, type);
	}

	public EntityInventory(InventoryHolder holder, InventoryType type, int overrideId) {
		super(holder, type, overrideId);
	}

	@Override
	public void clear() {
		super.clear();

		this.setHelmet(null);
		this.setChestplate(null);
		this.setLeggings(null);
		this.setBoots(null);
	}

	@Override
	protected void onOpen(Player player) {
		ContainerOpenPacket packet = new ContainerOpenPacket();
		packet.setId((byte) this.getId());
		packet.setType(this.getType().getContainerType());
		if (this.getHolder() instanceof Entity entity) {
			packet.setBlockPosition(entity.getPosition().toInt());
			packet.setUniqueEntityId(entity.getId());
		}

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
		if (this.getHolder() instanceof Entity entity) {
			var packet = new MobArmorEquipmentPacket();
			packet.setRuntimeEntityId(entity.getId());
			packet.setHelmet(ItemUtils.toNetwork(this.helmet));
			packet.setChestplate(ItemUtils.toNetwork(this.chestplate));
			packet.setLeggings(ItemUtils.toNetwork(this.leggings));
			packet.setBoots(ItemUtils.toNetwork(this.boots));

			entity.getViewers().forEach(viewer -> viewer.sendPacket(packet));
		}
	}
}
