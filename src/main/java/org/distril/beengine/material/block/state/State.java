package org.distril.beengine.material.block.state;

import lombok.Getter;

@Getter
public abstract class State<V> {

	private final String property;
	private final V defaultValue;

	public State(String property, V defaultValue) {
		this.property = property;
		this.defaultValue = defaultValue;
	}
}
