package org.distril.beengine.util

import com.google.gson.JsonObject
import com.nukkitx.nbt.NbtMap
import com.nukkitx.nbt.NbtUtils
import com.nukkitx.protocol.bedrock.data.inventory.ItemData
import org.distril.beengine.material.Material
import org.distril.beengine.material.block.BlockPalette
import org.distril.beengine.material.item.Item
import org.distril.beengine.material.item.ItemPalette
import java.io.ByteArrayInputStream
import java.util.*

object ItemUtils {

	fun fromJSON(itemJson: JsonObject): ItemData {
		val itemData = ItemData.builder()

		val identifier = itemJson["id"].asString
		itemData.id(ItemPalette.getRuntimeId(identifier))

		itemJson["block_state_b64"]?.let {
			val blockNbt = decodeNbt(it.asString)
			itemData.blockRuntimeId(BlockPalette.getRuntimeId(blockNbt))
		}

		itemJson["blockRuntimeId"]?.asInt?.let {
			itemData.blockRuntimeId(it)
		}

		itemJson["damage"]?.asInt?.let {
			val meta = if (it and 0x7FFF == 0x7FFF) -1 else it
			itemData.damage(meta)
		}

		itemJson["nbt_b64"]?.let {
			itemData.tag(decodeNbt(it.asString))
		}

		return itemData.usingNetId(false).count(1).build()
	}

	private fun decodeNbt(base64: String?): NbtMap {
		if (base64 == null) return NbtMap.EMPTY

		val nbtData = Base64.getDecoder().decode(base64)
		NbtUtils.createReaderLE(ByteArrayInputStream(nbtData)).use { return it.readTag() as NbtMap }
	}

	fun getAirIfNull(item: Item?): Item = if (item == null || item.count <= 0) Item.AIR else item

	fun toNetwork(item: Item?): ItemData {
		val nonNullItem = getAirIfNull(item)
		return ItemData.builder()
			.id(nonNullItem.material.itemRuntimeId)
			.damage(nonNullItem.meta)
			.count(nonNullItem.count)
			.tag(nonNullItem.nbt)
			.canBreak(arrayOf<String>()) // todo
			.canPlace(arrayOf<String>()) // todo
			.blockingTicks(0)
			.blockRuntimeId(nonNullItem.blockRuntimeId)
			.netId(nonNullItem.networkId)
			.usingNetId(nonNullItem.networkId != 0).build()
	}

	fun fromNetwork(itemData: ItemData?): Item {
		if (itemData == null) return Item.AIR

		return Material.fromItemRuntimeId(itemData.id).getItem<Item>().apply {
			meta = itemData.damage
			count = itemData.count
			if (itemData.tag != null)
				nbt = itemData.tag
		}
	}
}
