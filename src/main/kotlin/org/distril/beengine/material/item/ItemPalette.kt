package org.distril.beengine.material.item

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nukkitx.protocol.bedrock.data.inventory.ItemData
import com.nukkitx.protocol.bedrock.packet.CreativeContentPacket
import com.nukkitx.protocol.bedrock.packet.StartGamePacket
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.distril.beengine.util.ItemUtils
import org.distril.beengine.util.Utils
import org.distril.beengine.util.Utils.gson
import java.io.InputStreamReader

object ItemPalette {

	val items = mutableMapOf<String, Int>()
	val inverseItems: Map<Int, String>
		get() = items.entries.associate { (k, v) -> v to k }

	val entries = mutableSetOf<StartGamePacket.ItemEntry>()

	val creativeItems = mutableMapOf<Int, ItemData>()

	val creativeContentPacket = CreativeContentPacket()

	suspend fun init() = coroutineScope {
		launch {
			InputStreamReader(Utils.getResource("data/runtime_item_states.json")).use {
				val itemsJsons = gson.fromJson(it, JsonArray::class.java)
				itemsJsons.forEach { element ->
					val itemJson = element.asJsonObject
					val identifier = itemJson["name"].asString
					val runtimeId = itemJson["id"].asInt

					items[identifier] = runtimeId

					entries.add(StartGamePacket.ItemEntry(identifier, runtimeId.toShort(), false))
				}
			}
		}

		launch {
			InputStreamReader(Utils.getResource("data/creative_items.json")).use {
				val itemsJsons = gson.fromJson(it, JsonObject::class.java).getAsJsonArray("items")
				var networkId = 0
				itemsJsons.forEach { element ->
					networkId++

					val itemJson = element.asJsonObject
					creativeItems[networkId] = ItemUtils.fromJSON(itemJson).toBuilder().netId(networkId).build()
				}

				creativeContentPacket.contents = creativeItems.values.toTypedArray()
			}
		}
	}

	fun getIdentifier(runtimeId: Int) = inverseItems[runtimeId]!!

	fun getRuntimeId(identifier: String) = items[identifier]!!

	fun getCreativeItem(itemNetworkId: Int) = ItemUtils.fromNetwork(creativeItems[itemNetworkId])
}
