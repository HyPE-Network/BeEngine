package org.distril.beengine.player.data.provider;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtUtils;
import org.distril.beengine.player.data.GameMode;
import org.distril.beengine.player.data.PlayerData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NBTPlayerDataProvider implements PlayerDataProvider {

	@Override
	public void save(UUID uuid, PlayerData data) throws IOException {
		var playerFile = this.resolvePlayerNBTFile(uuid);
		if (playerFile.exists() && !playerFile.delete()) {
			throw new IOException("Failed to delete existing player data file for " + uuid);
		}

		try (var writer = NbtUtils.createWriter(new FileOutputStream(playerFile))) {
			writer.writeTag(this.createPlayerSaveData(data));
		}
	}

	@Override
	public CompletableFuture<PlayerData> load(UUID uuid) {
		return CompletableFuture.supplyAsync(() -> {
			var playerFile = this.resolvePlayerNBTFile(uuid);

			PlayerData playerData = new PlayerData();
			if (playerFile.exists()) {
				try (var reader = NbtUtils.createReader(new FileInputStream(playerFile))) {
					playerData = this.getPlayerDataFormat((NbtMap) reader.readTag());
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			}

			return playerData;
		});
	}

	private File resolvePlayerNBTFile(UUID uuid) {
		return Paths.get("players", uuid.toString() + ".dat").toFile();
	}

	private NbtMap createPlayerSaveData(PlayerData data) {
		var location = data.getLocation();
		return NbtMap.builder()
				.putFloat("pitch", data.getPitch())
				.putFloat("yaw", data.getYaw())
				.putFloat("headYaw", data.getHeadYaw())
				.putFloat("x", location.getX())
				.putFloat("y", location.getY())
				.putFloat("z", location.getZ())
				.putString("worldName", location.getWorld().getWorldName())
				.putInt("gamemode", data.getGameMode().ordinal())
				.build();
	}

	private PlayerData readPlayerData(NbtMap data) {
		var playerData = new PlayerData();
		playerData.setPitch(data.getFloat("pitch"));
		playerData.setYaw(data.getFloat("yaw"));
		playerData.setHeadYaw(data.getFloat("headYaw"));

		var position = Vector3f.from(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
		var defaultWorld = Server.getInstance().getWorldRegistry().getDefaultWorld();
		// todo: Server.getInstance().getWorldRegistry().getWorld("worldName");
		playerData.setLocation(Location.from(position, defaultWorld));
		playerData.setGameMode(GameMode.values()[data.getInt("gamemode")]);

		return playerData;
	}
}
