package org.distril.beengine.command.parser;

import org.distril.beengine.command.CommandSender;

public class TargetParser extends Parser {

	public static final TargetParser INSTANCE = new TargetParser();

	@Override
	public String parse(CommandSender sender, String input) {
		if (input.equals("@s") && !sender.isConsole()) {
			return sender.getName();
		}

		return input;
	}
}
