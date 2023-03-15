package org.distril.beengine.command.impl;

import org.distril.beengine.command.Command;
import org.distril.beengine.command.CommandSender;
import org.distril.beengine.command.data.Args;
import org.distril.beengine.command.data.ArgumentType;
import org.distril.beengine.player.Player;

public class GameModeCommand extends Command {

	public GameModeCommand() {
		super("gamemode", "Sets a player's game mode.", "gm");
		this.setPermission("command.gamemode");

		this.addArguments(
				ArgumentType.GameMode("gameMode"),
				ArgumentType.Target("player", true)
		);

		this.addArguments(
				ArgumentType.Integer("gameMode"),
				ArgumentType.Target("player", true)
		);
	}

	@Override
	public void execute(CommandSender sender, Args args) {
		if (!sender.hasPermission(this.getPermission())) {
			return;
		}

		if (args.isEmpty()) {
			sender.sendMessage("Use: /gamemode <gameMode> [player]");
			return;
		}

		var gameMode = args.getGameMode("gameMode");
		if (gameMode == null) {
			sender.sendMessage("Game mode '" + args.getString("gameMode") + "' is invalid");
			return;
		}


		var target = args.getTarget("player");
		if (sender.isConsole() && (!args.has("player") || target == null)) {
			sender.sendMessage("No targets matched selector");
			return;
		}

		if (target == null) {
			target = (Player) sender;
		}

		if (!target.equals(sender)) {
			sender.sendMessage("Set " + target.getUsername() + "'s game mode to " + gameMode.getIdentifier());
		} else {
			sender.sendMessage("Set own game mode to " + gameMode.getIdentifier());
		}

		target.setGameMode(gameMode);
		target.sendMessage("Your game mode has been updated to " + gameMode.getIdentifier());
	}
}
