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
	private boolean onlineModeEnabled;
	private boolean encryptionEnabled;
	private boolean debugEnabled;

	private String ip;
	private int port;
	private int compressionLevel;

	public ServerSettings(Path path) {
		this.config = Config.load(path.toFile());

		this.motd = this.config.get("server.motd", String.class);
		this.maximumPlayers = this.config.get("server.max-players", Integer.class);
		this.onlineModeEnabled = this.config.get("server.online-mode", Boolean.class);
		this.encryptionEnabled = this.config.get("server.enable-encryption", Boolean.class);
		this.debugEnabled = this.config.get("server.debug", Boolean.class);

		this.ip = this.config.get("network.ip", String.class);
		this.port = this.config.get("network.port", Integer.class);
		this.compressionLevel = this.config.get("network.compression-level", Integer.class);
	}

	public void save() {
		this.config.save();
	}
}
