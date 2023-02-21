package org.distril.beengine.command;

import com.nukkitx.protocol.bedrock.data.command.CommandData;
import com.nukkitx.protocol.bedrock.data.command.CommandEnumData;
import com.nukkitx.protocol.bedrock.data.command.CommandParamData;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.command.data.Args;
import org.distril.beengine.command.data.CommandArgument;
import org.distril.beengine.command.parser.EnumParser;
import org.distril.beengine.command.parser.Parser;

import java.util.*;

@Getter
public abstract class Command {

	private final List<CommandArgument[]> arguments = new ArrayList<>();

	private final List<Map<String, Parser>> parsers = new LinkedList<>();

	private final String name;
	private final String description;
	private final String[] aliases;

	@Setter
	private String permission;

	public Command(String name, String description, String... aliases) {
		this.name = name;
		this.description = description;

		String[] result = new String[aliases.length + 1];
		result[0] = name;
		System.arraycopy(aliases, 0, result, 1, aliases.length);

		this.aliases = result;
	}

	public abstract void execute(CommandSender sender, Args args);

	public void addArguments(CommandArgument... arguments) {
		this.arguments.add(arguments);

		Map<String, Parser> parsers = new LinkedHashMap<>();
		for (CommandArgument argument : arguments) {
			var parser = argument.getParser();
			parser.setOptional(argument.isOptional());

			if (parser instanceof EnumParser enumParser) {
				enumParser.setValues(argument.getEnumData());
			}

			parsers.put(argument.getName(), parser);
		}

		this.parsers.add(parsers);
	}

	public CommandData toNetwork() {
		CommandParamData[][] parametersData = new CommandParamData[this.arguments.size()][];

		for (int i = 0; i < parametersData.length; i++) {
			CommandArgument[] parameters = this.arguments.get(i);

			parametersData[i] = Arrays.stream(parameters)
					.map(CommandArgument::toNetwork)
					.toArray(CommandParamData[]::new);
		}

		var aliases = new CommandEnumData(this.name + "Aliases", this.aliases, false);

		return new CommandData(this.name, this.description, Collections.emptyList(), 0, aliases, parametersData);
	}
}
