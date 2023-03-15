package org.distril.beengine.material.block.state;

public class IntState extends State<Integer> {

	public IntState(String property) {
		this(property, 0);
	}

	public IntState(String property, int defaultValue) {
		super(property, defaultValue);
	}
}
