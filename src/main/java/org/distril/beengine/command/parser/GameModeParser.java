package org.distril.beengine.command.parser;

import org.distril.beengine.command.CommandSender;
import org.distril.beengine.player.data.Gamemode;

public class GameModeParser extends Parser {

	public static final GameModeParser INSTANCE = new GameModeParser();

	@Override
	public Gamemode parse(CommandSender sender, String input) {
		return Gamemode.fromIdOrAlias(input);
	}
}
