package org.distril.beengine.inventory.impl

import com.nukkitx.protocol.bedrock.packet.ContainerOpenPacket
import com.nukkitx.protocol.bedrock.packet.MobArmorEquipmentPacket
import com.nukkitx.protocol.bedrock.packet.MobEquipmentPacket
import org.distril.beengine.entity.EntityCreature
import org.distril.beengine.inventory.Inventory
import org.distril.beengine.inventory.InventoryHolder
import org.distril.beengine.inventory.InventoryType
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player
import org.distril.beengine.util.ItemUtils
import kotlin.reflect.KProperty

open class CreatureInventory(
	holder: InventoryHolder,
	overrideId: Int? = null
) : Inventory(holder, InventoryType.PLAYER, overrideId) {

	override val holder = super.holder as EntityCreature

	var helmet: Item? by ArmorDelegate()
	var chestplate: Item? by ArmorDelegate()
	var leggings: Item? by ArmorDelegate()
	var boots: Item? by ArmorDelegate()

	var heldItemIndex: Int = 0
		set(value) {
			if (value in 0..HOTBAR_SIZE) {
				field = value

				this.sendHeldItem(this.holder.viewers)
			}
		}

	override fun clear() {
		super.clear()

		this.helmet = null
		this.chestplate = null
		this.leggings = null
		this.boots = null

		this.setItemInHand(null)
	}

	fun getItemInHand() = this.getItem(this.heldItemIndex)

	fun setItemInHand(item: Item?) {
		this.setItem(this.heldItemIndex, item)
	}

	fun sendHeldItem(players: Collection<Player>) {
		val packet = MobEquipmentPacket()
		packet.item = ItemUtils.toNetwork(this.getItemInHand())
		packet.inventorySlot = this.heldItemIndex
		packet.hotbarSlot = packet.inventorySlot

		players.forEach {
			packet.runtimeEntityId = this.holder.id
			if (it == this.holder) {
				packet.runtimeEntityId = it.id
				this.sendSlot(this.heldItemIndex, it)
			}

			it.sendPacket(packet)
		}
	}

	override fun onOpen(player: Player) {
		val packet = ContainerOpenPacket()
		packet.id = this.id.toByte()
		packet.type = this.type.containerType

		player.sendPacket(packet)
	}

	override fun sendSlots(player: Player) {
		super.sendSlots(player)

		this.sendArmor()
	}

	protected fun sendArmor() {
		val packet = MobArmorEquipmentPacket()
		packet.runtimeEntityId = this.holder.id
		packet.helmet = ItemUtils.toNetwork(helmet)
		packet.chestplate = ItemUtils.toNetwork(chestplate)
		packet.leggings = ItemUtils.toNetwork(leggings)
		packet.boots = ItemUtils.toNetwork(boots)

		this.holder.viewers.forEach { it.sendPacket(packet) }
	}

	companion object {

		private const val HOTBAR_SIZE = 9
	}

	inner class ArmorDelegate {

		private var field: Item? = null

		operator fun getValue(thisRef: Any?, property: KProperty<*>) = ItemUtils.getAirIfNull(this.field)

		operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Item?) {
			this.field = value

			sendArmor()
		}
	}
}
