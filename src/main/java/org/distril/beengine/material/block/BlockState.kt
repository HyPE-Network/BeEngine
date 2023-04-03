package org.distril.beengine.material.block

import com.nukkitx.nbt.NbtMap
import org.distril.beengine.material.block.state.State

class BlockState(var states: NbtMap) {

	operator fun <T> set(state: State<T>, value: T) {
		val builder = this.states.getCompound("states").toBuilder()
		builder[state.property] = value
		this.states = states.toBuilder().putCompound("states", builder.build()).build()
	}

	@Suppress("UNCHECKED_CAST")
	operator fun <T> get(state: State<T>) = this.states[state.property] as T

	val runtimeId = BlockPalette.getRuntimeId(this.states)

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BlockState) return false

		return this.states == other.states
	}

	override fun hashCode() = this.states.hashCode()
}
