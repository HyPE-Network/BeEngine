package org.distril.beengine.command.impl;

import org.distril.beengine.command.Command;
import org.distril.beengine.command.CommandSender;
import org.distril.beengine.command.data.CommandArgument;

import java.util.Map;

public class GamemodeCommand extends Command {

	public GamemodeCommand() {
		super("gamemode", "set gamemode", "gm");
		this.setPermission("command.gamemode");

		this.addArguments(
				CommandArgument.gameMode("gameMode"),
				CommandArgument.target("player", true)
		);

		this.addArguments(
				CommandArgument.integer("gameMode"),
				CommandArgument.target("player", true)
		);
	}

	@Override
	public void execute(CommandSender sender, Map<String, Object> args) {
		sender.sendMessage(args.toString());
	}
}
