package org.distril.beengine.material.block.state;

public class StringState extends State<String> {

	public StringState(String property, String defaultValue) {
		super(property, defaultValue);
	}

	public StringState(String property, Enum<?> defaultValue) {
		this(property, defaultValue.name());
	}
}
