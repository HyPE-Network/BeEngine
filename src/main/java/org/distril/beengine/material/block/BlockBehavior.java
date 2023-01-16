package org.distril.beengine.material.block;

public interface BlockBehavior {

	default boolean canBeReplaced(Block block) {
		return false;
	}
}
