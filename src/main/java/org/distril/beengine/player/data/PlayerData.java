package org.distril.beengine.player.data;

import com.nukkitx.math.vector.Vector3f;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlayerData {

	private float pitch, yaw, headYaw;
	private Vector3f position = Vector3f.from(0, 60, 0);
	private Gamemode gamemode = Gamemode.SURVIVAL;
}
