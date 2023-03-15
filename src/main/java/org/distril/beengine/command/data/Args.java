package org.distril.beengine.command.data;

import lombok.AllArgsConstructor;
import org.distril.beengine.player.Player;
import org.distril.beengine.player.data.GameMode;
import org.distril.beengine.server.Server;

import java.util.Map;
import java.util.Random;

@AllArgsConstructor
public class Args {

	private final Map<String, String> args;

	public Integer getInteger(String key) {
		try {
			return Integer.parseInt(this.args.get(key));
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	public Float getFloat(String key) {
		try {
			return Float.parseFloat(this.args.get(key));
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	public Player getTarget(String key) {
		var username = this.getString(key);

		var server = Server.getInstance();
		if (username != null) {
			var players = server.getPlayers();
			if (username.equals("@r")) {
				return players.stream().toList().get(new Random().nextInt(players.size()));
			}
		}

		return server.getPlayer(username);
	}

	public String getString(String key) {
		return this.args.get(key);
	}

	public GameMode getGameMode(String key) {
		var id = this.getInteger(key);
		if (id != null) {
			return GameMode.fromId(id);
		}

		return GameMode.fromIdentifierOrAlias(this.getString(key));
	}

	public boolean has(String key) {
		return this.args.containsKey(key);
	}

	public boolean isEmpty() {
		return this.args.isEmpty();
	}
}
