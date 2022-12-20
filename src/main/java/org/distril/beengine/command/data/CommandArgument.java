package org.distril.beengine.command.data;

import com.nukkitx.protocol.bedrock.data.command.CommandEnumData;
import com.nukkitx.protocol.bedrock.data.command.CommandParam;
import com.nukkitx.protocol.bedrock.data.command.CommandParamData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.distril.beengine.command.parser.*;

import java.util.Collections;

@Getter
@AllArgsConstructor
public class CommandArgument {

	private final String name;
	private final CommandParam param;
	private final boolean optional;

	private final Parser parser;

	private final CommandEnumData enumData;

	public CommandArgument(String name, boolean optional, Parser parser, String... values) {
		this(name, CommandParam.TEXT, optional, parser, new CommandEnumData(name, values, false));
	}

	public CommandArgument(String name, CommandParam param, boolean optional, Parser parser) {
		this(name, param, optional, parser, null);
	}

	public static CommandArgument integer(String name) {
		return CommandArgument.integer(name, false);
	}

	public static CommandArgument integer(String name, boolean optional) {
		return new CommandArgument(name, CommandParam.INT, optional, IntegerParser.INSTANCE);
	}

	public static CommandArgument floats(String name) {
		return CommandArgument.floats(name, false);
	}

	public static CommandArgument floats(String name, boolean optional) {
		return new CommandArgument(name, CommandParam.FLOAT, optional, FloatParser.INSTANCE);
	}

	public static CommandArgument string(String name) {
		return CommandArgument.string(name, false);
	}

	public static CommandArgument string(String name, boolean optional) {
		return new CommandArgument(name, CommandParam.STRING, optional, StringParser.INSTANCE);
	}

	public static CommandArgument target(String name, boolean optional) {
		return new CommandArgument(name, CommandParam.TARGET, optional, StringParser.INSTANCE);
	}

	public static CommandArgument enums(String name, String... values) {
		return CommandArgument.enums(name, false, values);
	}

	public static CommandArgument enums(String name, boolean optional, String... values) {
		return new CommandArgument(name, optional, EnumParser.INSTANCE, values);
	}

	public static CommandArgument gameMode(String name) {
		return CommandArgument.gameMode(name, false);
	}

	public static CommandArgument gameMode(String name, boolean optional) {
		return CommandArgument.vanillaEnum(name, "GameMode", GameModeParser.INSTANCE, optional);
	}

	public static CommandArgument vanillaEnum(String name, String enumType, Parser parser, boolean optional) {
		return new CommandArgument(name, CommandParam.TEXT, optional, parser, new CommandEnumData(enumType,
				new String[0], false));
	}

	public CommandParamData toNetwork() {
		return new CommandParamData(this.name, this.optional, this.enumData, this.param, null, Collections.emptyList());
	}
}
