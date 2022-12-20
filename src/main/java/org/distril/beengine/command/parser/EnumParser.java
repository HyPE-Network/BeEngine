package org.distril.beengine.command.parser;

import com.nukkitx.protocol.bedrock.data.command.CommandEnumData;
import org.distril.beengine.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnumParser extends Parser {

	public static final EnumParser INSTANCE = new EnumParser();

	private final Set<String> values = new HashSet<>();

	public void setValues(CommandEnumData enumData) {
		this.values.addAll(Arrays.asList(enumData.getValues()));
	}

	@Override
	public String parse(CommandSender sender, String input) {
		if (this.values.contains(input)) {
			return input;
		}

		return null;
	}
}
