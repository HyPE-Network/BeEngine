package org.distril.beengine.command.parser;

import org.distril.beengine.command.CommandSender;

public class DefaultParser extends Parser {

	public static final DefaultParser INSTANCE = new DefaultParser();

	@Override
	public String parse(CommandSender sender, String input) {
		return input;
	}
}
