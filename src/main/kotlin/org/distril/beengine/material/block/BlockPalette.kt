package org.distril.beengine.material.block

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.nukkitx.nbt.NbtMap
import com.nukkitx.nbt.NbtType
import com.nukkitx.nbt.NbtUtils
import org.distril.beengine.material.Material
import org.distril.beengine.material.item.Item
import org.distril.beengine.server.Server
import org.distril.beengine.util.Utils

object BlockPalette {

	val states: BiMap<NbtMap, Int> = HashBiMap.create()
	val defaultStates = mutableMapOf<String, BlockState>()

	val meta2state = mutableMapOf<String, MutableList<BlockState>>()

	fun init() {
		NbtUtils.createGZIPReader(Utils.getResource("data/block_palette.nbt")).use {
			val nbtMap = it.readTag() as NbtMap

			for ((runtimeId, fullState) in nbtMap.getList("blocks", NbtType.COMPOUND).withIndex()) {
				val formattedState = this.formatState(fullState)

				this.states[formattedState] = runtimeId

				val identifier = formattedState.getString("name")
				this.defaultStates.putIfAbsent(identifier, BlockState(formattedState))

				this.meta2state.computeIfAbsent(identifier) { mutableListOf() }.add(BlockState(formattedState))
			}
		}
	}

	fun getBlockFullState(runtimeId: Int) = this.states.inverse().getOrDefault(runtimeId, NbtMap.EMPTY)

	fun getRuntimeId(identifier: String, states: NbtMap): Int {
		val fullState = NbtMap.builder().apply {
			this.putString("name", identifier)
			this.putCompound("states", states.getCompound("states"))
		}.build()

		return this.getRuntimeId(fullState)
	}

	fun getRuntimeId(fullState: NbtMap) = this.states.getOrDefault(this.formatState(fullState), 0)

	private fun formatState(states: NbtMap) = states.toBuilder().apply {
		this.remove("name_hash")
		this.remove("version")
	}.build()

	fun getDefaultState(material: Material) = this.defaultStates[material.identifier]!!

	fun getBlock(item: Item): Block {
		val blockStates = this.meta2state[item.material.identifier]!!
		return Server.blockRegistry.getBlockFromState(blockStates[item.meta])
	}

	fun isBlock(material: Material) = this.defaultStates.containsKey(material.identifier)
}
