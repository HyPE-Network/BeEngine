package org.distril.beengine.command.data;

import com.nukkitx.protocol.bedrock.data.command.CommandEnumData;
import com.nukkitx.protocol.bedrock.data.command.CommandParam;
import com.nukkitx.protocol.bedrock.data.command.CommandParamData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.distril.beengine.command.parser.EnumParser;
import org.distril.beengine.command.parser.Parser;
import org.distril.beengine.command.parser.StringParser;

import java.util.Collections;

@Getter
@AllArgsConstructor
public class CommandArgument {

	private final String name;
	private final CommandParam param;
	private final boolean optional;

	private final Parser parser;

	private final CommandEnumData enumData;

	public CommandArgument(String name, boolean optional, String... values) {
		this(name, CommandParam.TEXT, optional, EnumParser.INSTANCE, new CommandEnumData(name, values, false));
	}

	public CommandArgument(String name, CommandParam param, boolean optional) {
		this(name, param, optional, StringParser.INSTANCE, null);
	}

	public CommandArgument(String name, CommandParam param, Parser parser, boolean optional) {
		this(name, param, optional, parser, null);
	}

	public CommandParamData toNetwork() {
		return new CommandParamData(this.name, this.optional, this.enumData, this.param, null, Collections.emptyList());
	}
}
