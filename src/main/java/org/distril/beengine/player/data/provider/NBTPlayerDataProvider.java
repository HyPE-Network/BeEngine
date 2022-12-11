package org.distril.beengine.player.data.provider;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtUtils;
import org.distril.beengine.player.data.Gamemode;
import org.distril.beengine.player.data.PlayerData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

public class NBTPlayerDataProvider implements PlayerDataProvider {

	@Override
	public void save(UUID uuid, PlayerData data) throws IOException {
		File playerFile = this.resolvePlayerNBTFile(uuid);
		if (playerFile.exists() && !playerFile.delete()) {
			throw new IOException("Failed to delete existing player data file for " + uuid);
		}

		try (var writer = NbtUtils.createWriter(new FileOutputStream(playerFile))) {
			writer.writeTag(this.getPlayerNBTDataFormat(data));
		}
	}

	@Override
	public PlayerData load(UUID uuid) throws IOException {
		File playerFile = this.resolvePlayerNBTFile(uuid);

		PlayerData playerData = null;
		if (playerFile.exists()) {
			try (var reader = NbtUtils.createReader(new FileInputStream(playerFile))) {
				playerData = this.getPlayerDataFormat((NbtMap) reader.readTag());
			}
		}

		return playerData;
	}

	private File resolvePlayerNBTFile(UUID uuid) {
		return Paths.get("players", uuid.toString() + ".dat").toFile();
	}

	private NbtMap getPlayerNBTDataFormat(PlayerData data) {
		// todo
		return NbtMap.builder()
				.putFloat("pitch", data.getPitch())
				.putFloat("yaw", data.getYaw())
				.putFloat("headYaw", data.getHeadYaw())
				.putFloat("x", data.getPosition().getX())
				.putFloat("y", data.getPosition().getY())
				.putFloat("z", data.getPosition().getZ())
				.putInt("gamemode", data.getGamemode().ordinal())
				.build();
	}

	private PlayerData getPlayerDataFormat(NbtMap data) {
		var playerData = new PlayerData();
		playerData.setPitch(data.getFloat("pitch"));
		playerData.setYaw(data.getFloat("yaw"));
		playerData.setHeadYaw(data.getFloat("headYaw"));
		playerData.setPosition(Vector3f.from(data.getFloat("x"), data.getFloat("y"), data.getFloat("z")));
		playerData.setGamemode(Gamemode.values()[data.getInt("gamemode")]);

		return playerData;
	}
}
