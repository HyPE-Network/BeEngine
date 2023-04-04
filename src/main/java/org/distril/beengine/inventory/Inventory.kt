package org.distril.beengine.inventory

import com.nukkitx.protocol.bedrock.packet.ContainerClosePacket
import com.nukkitx.protocol.bedrock.packet.InventoryContentPacket
import com.nukkitx.protocol.bedrock.packet.InventorySlotPacket
import org.distril.beengine.material.Material
import org.distril.beengine.material.item.Item
import org.distril.beengine.player.Player
import org.distril.beengine.util.ItemUtils
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

abstract class Inventory(
	open val holder: InventoryHolder,
	val type: InventoryType,
	overrideId: Int? = null
) {

	val viewers = mutableSetOf<Player>()

	private val items = arrayOfNulls<Item>(type.size)

	protected val id = overrideId ?: NEXT_ID.incrementAndGet()

	fun setItem(slot: Int, item: Item?) = this.setItem(slot, item, true)

	open fun setItem(slot: Int, item: Item?, send: Boolean): Boolean {
		if (slot < 0 || slot >= this.items.size) return false

		this.items[slot] = item

		if (send) this.onSlotChange(slot)

		return true
	}

	open fun getItem(slot: Int): Item {
		return if (slot < 0 || slot >= this.items.size) Material.AIR.getItem()
		else ItemUtils.getAirIfNull(this.items[slot])
	}

	fun addItem(item: Item) {
		this.items.indices.forEach {
			if (this.items[it] == null) {
				this.setItem(it, item)
				return
			}
		}
	}

	open fun clear() = this.clear(true)

	fun clear(send: Boolean) {
		Arrays.fill(this.items, null)

		if (send) this.sendSlots()
	}

	fun openFor(player: Player): Boolean {
		if (this.viewers.add(player)) {
			this.onOpen(player)
			return true
		}

		return false
	}

	protected open fun onOpen(player: Player) {
		/**/
	}

	fun closeFor(player: Player): Boolean {
		if (this.viewers.remove(player)) {
			this.onClose(player)
			return true
		}

		return false
	}

	protected fun onClose(player: Player) {
		val packet = ContainerClosePacket()
		packet.id = this.id.toByte()

		player.sendPacket(packet)
	}

	fun onSlotChange(slot: Int) = this.sendSlot(slot, *this.viewers.toTypedArray())

	protected open fun sendSlot(slot: Int, vararg players: Player) {
		val packet = InventorySlotPacket()
		packet.containerId = this.id
		packet.slot = slot
		packet.item = ItemUtils.toNetwork(this.getItem(slot))

		players.forEach { it.sendPacket(packet) }
	}

	open fun sendSlots(player: Player) {
		val packet = InventoryContentPacket()
		packet.containerId = this.id
		packet.contents = this.items.map { ItemUtils.toNetwork(ItemUtils.getAirIfNull(it)) }

		player.sendPacket(packet)
	}

	fun sendSlots() = this.viewers.forEach { this.sendSlots(it) }

	companion object {

		private val NEXT_ID = AtomicInteger(0)
	}
}
