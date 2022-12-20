package org.distril.beengine.command.parser;

import org.distril.beengine.command.CommandSender;

public class StringParser extends Parser {

	public static final StringParser INSTANCE = new StringParser();

	@Override
	public String parse(CommandSender sender, String input) {
		return input;
	}
}
