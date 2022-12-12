package org.distril.beengine.util;

import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtUtils;
import org.distril.beengine.Bootstrap;

import java.io.IOException;

public class BedrockResourceLoader {

	public static final NbtMap BIOME_DEFINITIONS;
	public static final NbtMap ENTITY_IDENTIFIERS;

	static {
		try (var reader = NbtUtils.createNetworkReader(Bootstrap.getResource("data/biome_definitions.dat"))) {
			BIOME_DEFINITIONS = (NbtMap) reader.readTag();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		try (var reader = NbtUtils.createNetworkReader(Bootstrap.getResource("data/entity_identifiers.dat"))) {
			ENTITY_IDENTIFIERS = (NbtMap) reader.readTag();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
