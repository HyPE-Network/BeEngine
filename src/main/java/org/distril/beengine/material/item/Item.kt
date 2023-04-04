package org.distril.beengine.material.item

import com.nukkitx.nbt.NbtMap
import com.nukkitx.nbt.NbtType
import org.distril.beengine.material.Material
import org.distril.beengine.material.block.Block
import org.distril.beengine.material.block.BlockPalette
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min

abstract class Item(val material: Material) : Cloneable, ItemBehaviors {

	val networkId = if (this.material === Material.AIR) 0 else NEXT_NETWORK_ID.incrementAndGet()
	var meta = 0
	var count = 1
		set(value) {
			field = max(0, min(value, this.maxCount))
		}

	var nbt = NbtMap.EMPTY
		set(value) {
			field = value ?: NbtMap.EMPTY
		}

	val blockRuntimeId: Int
		get() = this.toBlock<Block>()?.state?.runtimeId ?: 0

	var customName: String?
		get() = this.nbt.getCompound("display").getString("Name")
		set(value) {
			this.nbt = if (value != null) {
				this.nbt.getCompound("display").toBuilder()
					.putString("Name", value)
					.build()
			} else {
				val displayBuilder = nbt.getCompound("display").toBuilder()
				displayBuilder.remove("Name")
				displayBuilder.build()
			}
		}

	var lores: List<String>
		get() = this.nbt.getCompound("display").getList("Lore", NbtType.STRING) ?: listOf()
		set(value) {
			this.nbt = if (value.isEmpty()) {
				val displayBuilder = this.nbt.getCompound("display").toBuilder()
				displayBuilder.remove("Lore")
				displayBuilder.build()
			} else {
				this.nbt.getCompound("display").toBuilder()
					.putList("Lore", NbtType.STRING, *value.toTypedArray())
					.build()
			}
		}

	@Suppress("UNCHECKED_CAST")
	fun <T : Block> toBlock(): T? = if (this.material.isBlock) BlockPalette.getBlock(this) as T else null

	public override fun clone(): Item {
		val clone = super.clone() as Item
		clone.meta = this.meta
		clone.count = this.count
		clone.nbt = this.nbt
		return clone
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Item) return false

		return this.equal(other)
	}

	fun equal(other: Item, checkMeta: Boolean = true, checkData: Boolean = true): Boolean {
		val metaValid = !checkMeta || this.meta == other.meta
		val dataValid = !checkData || this.nbt == other.nbt
		return this.material === other.material && metaValid && dataValid
	}

	override fun hashCode() = Objects.hash(this.material, this.meta, this.count, this.nbt)

	companion object {

		val AIR = Material.AIR.getItem<Item>()
			get() = field.clone()

		private val NEXT_NETWORK_ID = AtomicInteger(0)
	}
}
