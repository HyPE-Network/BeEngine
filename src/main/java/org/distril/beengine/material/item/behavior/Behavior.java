package org.distril.beengine.material.item.behavior;

public interface Behavior {

	default int getMaxStackSize() {
		return 64;
	}
}
