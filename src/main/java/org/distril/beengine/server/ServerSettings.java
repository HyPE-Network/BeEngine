package org.distril.beengine.server;

import lombok.Getter;

@Getter
public class ServerSettings {

	private final String ip = "0.0.0.0";
	private final int port = 19132;

	private final String motd = "test motd";

	private final int maximumPlayers = 100;
}
