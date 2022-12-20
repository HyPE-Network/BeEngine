package org.distril.beengine.command.parser;

import org.distril.beengine.command.CommandSender;

public class IntegerParser extends Parser {

	public static final IntegerParser INSTANCE = new IntegerParser();

	@Override
	public Integer parse(CommandSender sender, String input) {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException exception) {
			return null;
		}
	}
}
