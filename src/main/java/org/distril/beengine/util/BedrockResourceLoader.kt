package org.distril.beengine.util

import com.nukkitx.nbt.NbtMap
import com.nukkitx.nbt.NbtUtils
import com.nukkitx.protocol.bedrock.packet.AvailableEntityIdentifiersPacket
import com.nukkitx.protocol.bedrock.packet.BiomeDefinitionListPacket
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object BedrockResourceLoader {

	val biomeDefinitionListPacket = BiomeDefinitionListPacket()

	val availableEntityIdentifiersPacket = AvailableEntityIdentifiersPacket()

	suspend fun init() = coroutineScope {
		launch {
			NbtUtils.createNetworkReader(Utils.getResource("data/biome_definitions.dat")).use {
				biomeDefinitionListPacket.definitions = it.readTag() as NbtMap
			}
		}

		launch {
			NbtUtils.createNetworkReader(Utils.getResource("data/entity_identifiers.dat")).use {
				availableEntityIdentifiersPacket.identifiers = it.readTag() as NbtMap
			}
		}
	}
}
