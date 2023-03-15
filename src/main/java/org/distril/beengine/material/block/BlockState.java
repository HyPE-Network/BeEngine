package org.distril.beengine.material.block;

import com.nukkitx.nbt.NbtMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.distril.beengine.material.block.state.State;

@Getter
@AllArgsConstructor
public class BlockState {

	private NbtMap states;

	public <T> void set(State<T> state, T value) {
		var builder = this.states.getCompound("states").toBuilder();
		builder.put(state.getProperty(), value);
		this.states = this.states.toBuilder().putCompound("states", builder.build()).build();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(State<T> state) {
		return (T) this.states.get(state.getProperty());
	}

	public int getRuntimeId() {
		return BlockPalette.getRuntimeId(this.states);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		BlockState that = (BlockState) obj;
		return this.states.equals(that.getStates());
	}

	@Override
	public int hashCode() {
		return this.states.hashCode();
	}
}
