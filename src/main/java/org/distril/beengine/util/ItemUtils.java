package org.distril.beengine.util;

import com.google.gson.JsonObject;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtUtils;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import org.distril.beengine.material.block.BlockPalette;
import org.distril.beengine.material.item.ItemPalette;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class ItemUtils {

	public static ItemData fromJSON(JsonObject itemJSON) throws IOException {
		var identifier = itemJSON.get("id").getAsString();
		var count = itemJSON.has("count") ? itemJSON.get("count").getAsInt() : 1;
		var meta = itemJSON.has("damage") && itemJSON.get("damage").getAsShort() != Short.MAX_VALUE
		           ? itemJSON.get("damage").getAsInt() : 0;
		var blockStateNbt = itemJSON.has("block_state_b64") ? itemJSON.get("block_state_b64").getAsString() : null;
		var tagNbt = itemJSON.has("nbt_b64") ? itemJSON.get("nbt_b64").getAsString() : null;

		int blockRuntimeId = 0;

		if (blockStateNbt != null) {
			var blockNbt = ItemUtils.base64ToNbt(blockStateNbt).toBuilder();
			blockNbt.remove("name_hash");
			blockNbt.remove("version");

			blockRuntimeId = BlockPalette.getRuntimeId(identifier, blockNbt.build());
		}

		return ItemData.builder()
				.id(ItemPalette.getRuntimeId(identifier))
				.count(count)
				.damage(meta)
				.blockRuntimeId(blockRuntimeId)
				.tag(ItemUtils.base64ToNbt(tagNbt))
				.build();
	}

	private static NbtMap base64ToNbt(String base64) throws IOException {
		if (base64 == null) {
			return NbtMap.EMPTY;
		}

		var nbtData = Base64.getDecoder().decode(base64);
		try (var reader = NbtUtils.createReaderLE(new ByteArrayInputStream(nbtData))) {
			return ((NbtMap) reader.readTag());
		}
	}
}
