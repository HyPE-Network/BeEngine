package org.distril.beengine.server;

import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.config.Config;

import java.nio.file.Path;

@Getter
@Setter
public class ServerSettings {

	private final Config config;

	private String motd;
	private int maximumPlayers;
	private boolean xboxRequired;
	private boolean encryptionEnabled;
	private boolean debugEnabled;

	private String ip;
	private int port;
	private int compressionLevel;

	private String defaultWorldName;

	private int chunkExpiryTime;

	public ServerSettings(Path path) {
		this.config = Config.load(path.toFile());

		this.motd = this.config.get("server.motd", String.class);
		this.maximumPlayers = this.config.get("server.max-players", Integer.class);
		this.xboxRequired = this.config.get("server.xbox-require", Boolean.class);
		this.encryptionEnabled = this.config.get("server.enable-encryption", Boolean.class);
		this.debugEnabled = this.config.get("server.debug", Boolean.class);

		this.ip = this.config.get("network.ip", String.class);
		this.port = this.config.get("network.port", Integer.class);
		this.compressionLevel = this.config.get("network.compression-level", Integer.class);

		this.defaultWorldName = this.config.get("world.default", String.class);

		this.chunkExpiryTime = this.config.get("world.chunk.expiry-time", Integer.class);
	}

	public void save() {
		this.config.save();
	}
}
