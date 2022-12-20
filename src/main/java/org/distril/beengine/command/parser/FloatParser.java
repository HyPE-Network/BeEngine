package org.distril.beengine.command.parser;

import org.distril.beengine.command.CommandSender;

public class FloatParser extends Parser {

	public static final FloatParser INSTANCE = new FloatParser();

	@Override
	public Float parse(CommandSender sender, String input) {
		try {
			return Float.parseFloat(input);
		} catch (NumberFormatException exception) {
			return null;
		}
	}
}
