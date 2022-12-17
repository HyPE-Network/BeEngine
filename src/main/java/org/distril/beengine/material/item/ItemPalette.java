package org.distril.beengine.material.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.packet.CreativeContentPacket;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;
import org.distril.beengine.Bootstrap;
import org.distril.beengine.util.ItemUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemPalette {

	private static final Gson GSON = new Gson();

	private static final BiMap<String, Integer> ITEMS = HashBiMap.create();
	private static final Set<StartGamePacket.ItemEntry> ENTRIES = new HashSet<>();

	private static final CreativeContentPacket CREATIVE_CONTENT_PACKET = new CreativeContentPacket();

	static {
		try (var reader = new InputStreamReader(Bootstrap.getResource("data/runtime_item_states.json"))) {
			var items = GSON.fromJson(reader, JsonArray.class);

			items.forEach(item -> {
				var itemObj = item.getAsJsonObject();
				var identifier = itemObj.get("name").getAsString();
				var runtimeId = itemObj.get("id").getAsInt();
				ITEMS.put(identifier, runtimeId);
				ENTRIES.add(new StartGamePacket.ItemEntry(identifier, (short) runtimeId, false));
			});
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		try (var reader = new InputStreamReader(Bootstrap.getResource("data/creative_items.json"))) {
			var itemArray = GSON.fromJson(reader, JsonObject.class).getAsJsonArray("items");

			List<ItemData> itemsData = new ArrayList<>();
			int netId = 0;
			for (var itemElement : itemArray) {
				var itemObj = itemElement.getAsJsonObject();

				netId++;
				itemsData.add(
						ItemUtils.fromJSON(itemObj)
								.toBuilder()
								.netId(netId)
								.build()
				);
			}


			CREATIVE_CONTENT_PACKET.setContents(itemsData.toArray(new ItemData[0]));
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public static List<StartGamePacket.ItemEntry> getItemEntries() {
		return ENTRIES.stream().toList();
	}

	public static CreativeContentPacket getCreativeContentPacket() {
		return CREATIVE_CONTENT_PACKET;
	}

	public static String getIdentifier(int runtimeId) {
		return ITEMS.inverse().get(runtimeId);
	}

	public static int getRuntimeId(String identifier) {
		return ITEMS.get(identifier);
	}
}
