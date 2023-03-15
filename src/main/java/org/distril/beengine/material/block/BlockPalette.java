package org.distril.beengine.material.block;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtType;
import com.nukkitx.nbt.NbtUtils;
import org.distril.beengine.Bootstrap;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.server.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPalette {

	private static final BiMap<NbtMap, Integer> STATES = HashBiMap.create();

	private static final Map<String, BlockState> DEFAULT_STATES = new HashMap<>();
	private static final Map<String, List<BlockState>> META2STATE = new HashMap<>();

	static {
		try (var reader = NbtUtils.createGZIPReader(Bootstrap.getResource("data/block_palette.nbt"))) {
			var nbtMap = (NbtMap) reader.readTag();
			int runtimeId = 0;
			for (var fullState : nbtMap.getList("blocks", NbtType.COMPOUND)) {
				var builder = fullState.toBuilder();
				builder.remove("name_hash");
				builder.remove("version");
				fullState = builder.build();

				STATES.put(fullState, runtimeId);

				var identifier = fullState.getString("name");
				DEFAULT_STATES.putIfAbsent(identifier, new BlockState(fullState));

				META2STATE.computeIfAbsent(identifier, $ -> new ArrayList<>()).add(new BlockState(fullState));

				runtimeId++;
			}
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public static NbtMap getBlockFullState(int runtimeId) {
		return STATES.inverse().getOrDefault(runtimeId, NbtMap.EMPTY);
	}

	public static int getRuntimeId(String identifier, NbtMap states) {
		var fullState = NbtMap.builder();
		fullState.putString("name", identifier);
		fullState.putCompound("states", states.getCompound("states"));
		return BlockPalette.getRuntimeId(fullState.build());
	}

	public static int getRuntimeId(NbtMap fullState) {
		var builder = fullState.toBuilder();
		builder.remove("name_hash");
		builder.remove("version");
		return STATES.getOrDefault(builder.build(), 0);
	}

	public static BlockState getDefaultState(Material material) {
		return DEFAULT_STATES.get(material.getIdentifier());
	}

	public static Block getBlock(Item item) {
		var blockStates = META2STATE.get(item.getMaterial().getIdentifier());

		return Server.getInstance().getBlockRegistry().getBlockFromState(blockStates.get(item.getMeta()));
	}
}
