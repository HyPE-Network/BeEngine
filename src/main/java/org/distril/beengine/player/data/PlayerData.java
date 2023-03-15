package org.distril.beengine.player.data;

import com.nukkitx.math.vector.Vector3f;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.server.Server;
import org.distril.beengine.world.util.Location;

@Setter
@Getter
public class PlayerData {

	private float pitch, yaw, headYaw;
	private Location location = Location.from(Vector3f.from(0, 60, 0),
			Server.getInstance().getWorldRegistry().getDefaultWorld());
	private GameMode gameMode = GameMode.CREATIVE;
}
