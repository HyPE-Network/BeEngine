package org.distril.beengine.material.block;

import com.nukkitx.math.vector.Vector3f;
import org.distril.beengine.material.item.Item;
import org.distril.beengine.player.Player;
import org.distril.beengine.util.Direction;

public interface BlockBehavior {

	default boolean onPlace(Item item, Block target, Direction blockFace, Vector3f clickPosition, Player player) {
		return true;
	}

	default boolean canBeReplaced(Block block) {
		return false;
	}
}
