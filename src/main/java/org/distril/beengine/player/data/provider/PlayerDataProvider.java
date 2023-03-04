package org.distril.beengine.player.data.provider;

import org.distril.beengine.player.data.PlayerData;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataProvider {

	void save(UUID uuid, PlayerData data) throws IOException;

	CompletableFuture<PlayerData> load(UUID uuid);
}
