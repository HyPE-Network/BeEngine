package org.distril.beengine.player.data.provider;

import org.distril.beengine.player.data.PlayerData;

import java.io.IOException;
import java.util.UUID;

public interface PlayerDataProvider {

	void save(UUID uuid, PlayerData data) throws IOException;

	PlayerData load(UUID uuid) throws IOException;
}
