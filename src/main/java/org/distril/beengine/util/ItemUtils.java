package org.distril.beengine.util;

import com.google.gson.JsonObject;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtUtils;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.block.BlockPalette;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.material.item.ItemBuilder;
import org.distril.beengine.material.item.ItemPalette;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class ItemUtils {

	public static ItemData fromJSON(JsonObject itemJSON) throws IOException {
		var itemData = ItemData.builder();
		var identifier = itemJSON.get("id").getAsString();
		itemData.id(ItemPalette.getRuntimeId(identifier));

		if (itemJSON.has("block_state_b64")) {
			var blockNbt = ItemUtils.decodeNbt(itemJSON.get("block_state_b64").getAsString());

			itemData.blockRuntimeId(BlockPalette.getRuntimeId(blockNbt));
		}

		if (itemJSON.has("blockRuntimeId")) {
			itemData.blockRuntimeId(itemJSON.get("blockRuntimeId").getAsInt());
		}

		if (itemJSON.has("damage")) {
			var meta = itemJSON.get("damage").getAsInt();
			if ((meta & 0x7FFF) == 0x7FFF) {
				meta = -1;
			}

			itemData.damage(meta);
		}

		if (itemJSON.has("nbt_b64")) {
			itemData.tag(ItemUtils.decodeNbt(itemJSON.get("nbt_b64").getAsString()));
		}

		return itemData.usingNetId(false)
				.count(1).build();
	}

	private static NbtMap decodeNbt(String base64) throws IOException {
		if (base64 == null) {
			return NbtMap.EMPTY;
		}

		var nbtData = Base64.getDecoder().decode(base64);
		try (var reader = NbtUtils.createReaderLE(new ByteArrayInputStream(nbtData))) {
			return ((NbtMap) reader.readTag());
		}
	}

	public static Item getAirIfNull(Item item) {
		if (item == null || item.getCount() <= 0) {
			return Item.AIR;
		}

		return item;
	}

	public static ItemData toNetwork(Item item) {
		item = ItemUtils.getAirIfNull(item);

		return ItemData.builder()
				.id(item.getRuntimeId())
				.damage(item.getMeta())
				.count(item.getCount())
				.tag(item.getNbt())
				.canBreak(new String[0]) // todo
				.canPlace(new String[0]) // todo
				.blockingTicks(item.getBlockingTicks())
				.blockRuntimeId(item.getBlockRuntimeId())
				.netId(item.getNetworkId())
				.usingNetId(item.getNetworkId() != 0)
				.build();
	}

	public static Item fromNetwork(ItemData itemData) {
		if (itemData == null) {
			return Item.AIR;
		}

		var builder = ItemBuilder.builder(Material.fromItemRuntimeId(itemData.getId()));
		builder.meta(itemData.getDamage());
		builder.count(itemData.getCount());
		builder.nbt(itemData.getTag());
		builder.blockingTicks(itemData.getBlockingTicks());
		builder.blockRuntimeId(itemData.getBlockRuntimeId());
		builder.networkId(itemData.getNetId());

		return builder.build();
	}
}
