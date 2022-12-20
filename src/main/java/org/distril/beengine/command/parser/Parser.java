package org.distril.beengine.command.parser;

import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.command.CommandSender;

@Getter
@Setter
public abstract class Parser {

	private boolean optional;

	public abstract Object parse(CommandSender sender, String input);
}
