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
		if (itemJson.has("block_state_b64")) {
			val blockNbt = decodeNbt(itemJson["block_state_b64"].asString)
			itemData.blockRuntimeId(BlockPalette.getRuntimeId(blockNbt))
		}

		if (itemJson.has("blockRuntimeId")) itemData.blockRuntimeId(itemJson["blockRuntimeId"].asInt)

		if (itemJson.has("damage")) {
			var meta = itemJson["damage"].asInt
			if (meta and 0x7FFF == 0x7FFF) {
				meta = -1
			}

			itemData.damage(meta)
		}

		if (itemJson.has("nbt_b64")) itemData.tag(decodeNbt(itemJson["nbt_b64"].asString))

		return itemData.usingNetId(false)
			.count(1).build()
	}

	private fun decodeNbt(base64: String?): NbtMap {
		if (base64 == null) return NbtMap.EMPTY

		val nbtData = Base64.getDecoder().decode(base64)
		NbtUtils.createReaderLE(ByteArrayInputStream(nbtData)).use { return it.readTag() as NbtMap }
	}

	fun getAirIfNull(item: Item?): Item = if (item == null || item.count <= 0) Material.AIR.getItem() else item

	fun toNetwork(item: Item): ItemData {
		return ItemData.builder()
			.id(item.material.itemRuntimeId)
			.damage(item.meta)
			.count(item.count)
			.tag(item.nbt)
			.canBreak(listOf<String>().toTypedArray()) // todo
			.canPlace(listOf<String>().toTypedArray()) // todo
			.blockingTicks(0)
			.blockRuntimeId(item.blockRuntimeId)
			.netId(item.networkId)
			.usingNetId(item.networkId != 0)
			.build()
	}

	fun fromNetwork(itemData: ItemData?): Item {
		if (itemData == null) return Material.AIR.getItem()

		return Material.fromItemRuntimeId(itemData.id).getItem<Item>().apply {
			meta = itemData.damage
			count = itemData.count
			nbt = itemData.tag
		}
	}
}
