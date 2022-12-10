package org.distril.beengine.player.data.provider;

import com.nukkitx.nbt.NBTInputStream;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NBTPlayerDataProvider implements PlayerDataProvider {

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	@Override
	public void save(UUID uuid, PlayerData data) throws IOException {
		this.readWriteLock.writeLock().lock();
		try {
			File playerFile = this.resolvePlayerNBTFile(uuid);
			if (playerFile.exists() && !playerFile.delete()) {
				throw new IOException("Failed to delete existing player data file for " + uuid);
			}

			try (var outputStream = NbtUtils.createWriter(new FileOutputStream(playerFile))) {
				outputStream.writeTag(this.getPlayerNBTDataFormat(data));
			}
		} finally {
			this.readWriteLock.writeLock().lock();
		}
	}

	@Override
	public PlayerData load(UUID uuid) throws IOException {
		this.readWriteLock.readLock().lock();
		try {
			File playerFile = this.resolvePlayerNBTFile(uuid);

			PlayerData playerData = null;
			if (playerFile.exists()) {
				try (NBTInputStream inputStream = NbtUtils.createReader(new FileInputStream(playerFile))) {
					playerData = this.getPlayerDataFormat((NbtMap) inputStream.readTag());
				}
			}

			return playerData;
		} finally {
			this.readWriteLock.readLock().unlock();
		}
	}

	private File resolvePlayerNBTFile(UUID uuid) {
		return Paths.get("players", uuid.toString() + ".dat").toFile();
	}

	private NbtMap getPlayerNBTDataFormat(PlayerData data) {
		// todo
		return NbtMap.builder()
				.putInt("gamemode", data.getGamemode().ordinal())
				.build();
	}

	private PlayerData getPlayerDataFormat(NbtMap data) {
		var playerData = new PlayerData();
		playerData.setGamemode(Gamemode.values()[data.getInt("gamemode")]);

		return playerData;
	}
}
