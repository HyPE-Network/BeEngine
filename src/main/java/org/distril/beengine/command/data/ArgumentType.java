package org.distril.beengine.command.data;

import com.nukkitx.protocol.bedrock.data.command.CommandEnumData;
import com.nukkitx.protocol.bedrock.data.command.CommandParam;
import lombok.experimental.UtilityClass;
import org.distril.beengine.command.parser.StringParser;
import org.distril.beengine.command.parser.TargetParser;

@UtilityClass
public class ArgumentType {

	public static CommandArgument Integer(String name) {
		return ArgumentType.Integer(name, false);
	}

	public static CommandArgument Integer(String name, boolean optional) {
		return new CommandArgument(name, CommandParam.INT, optional);
	}

	public static CommandArgument Float(String name) {
		return ArgumentType.Float(name, false);
	}

	public static CommandArgument Float(String name, boolean optional) {
		return new CommandArgument(name, CommandParam.FLOAT, optional);
	}

	public static CommandArgument Target(String name) {
		return ArgumentType.Target(name, false);
	}

	public static CommandArgument Target(String name, boolean optional) {
		return new CommandArgument(name, CommandParam.TARGET, TargetParser.INSTANCE, optional);
	}

	public static CommandArgument String(String name) {
		return ArgumentType.String(name, false);
	}

	public static CommandArgument String(String name, boolean optional) {
		return new CommandArgument(name, CommandParam.STRING, optional);
	}

	public static CommandArgument Enum(String name, String... values) {
		return ArgumentType.Enum(name, false, values);
	}

	public static CommandArgument Enum(String name, boolean optional, String... values) {
		return new CommandArgument(name, optional, values);
	}

	public static CommandArgument GameMode(String name) {
		return ArgumentType.GameMode(name, false);
	}

	public static CommandArgument GameMode(String name, boolean optional) {
		return ArgumentType.MinecraftEnum(name, "GameMode", optional);
	}

	private static CommandArgument MinecraftEnum(String name, String enumType, boolean optional) {
		return new CommandArgument(name, CommandParam.TEXT, optional, StringParser.INSTANCE, new CommandEnumData(enumType,
				new String[0], false));
	}
}
