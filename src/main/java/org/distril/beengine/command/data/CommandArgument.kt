package org.distril.beengine.command.data

import com.nukkitx.protocol.bedrock.data.command.CommandEnumData
import com.nukkitx.protocol.bedrock.data.command.CommandParam
import com.nukkitx.protocol.bedrock.data.command.CommandParamData
import org.distril.beengine.command.parser.DefaultParser
import org.distril.beengine.command.parser.EnumParser
import org.distril.beengine.command.parser.Parser

class CommandArgument(
	val name: String,
	val param: CommandParam = CommandParam.TEXT,
	val optional: Boolean,
	val parser: Parser = DefaultParser,
	val enumData: CommandEnumData? = null
) {

	constructor(name: String, optional: Boolean, vararg values: String) : this(
		name, CommandParam.TEXT, optional, EnumParser, CommandEnumData(name, values, false)
	)

	fun toNetwork(): CommandParamData {
		return CommandParamData(name, optional, enumData, param, null, emptyList())
	}
}
