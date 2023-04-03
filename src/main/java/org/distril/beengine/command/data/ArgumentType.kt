package org.distril.beengine.command.data

import com.nukkitx.protocol.bedrock.data.command.CommandEnumData
import com.nukkitx.protocol.bedrock.data.command.CommandParam
import org.distril.beengine.command.parser.EnumParser
import org.distril.beengine.command.parser.TargetParser

object ArgumentType {

	fun integer(name: String, optional: Boolean = false) = CommandArgument(name, CommandParam.INT, optional)

	fun float(name: String, optional: Boolean = false) = CommandArgument(name, CommandParam.FLOAT, optional)

	fun target(name: String, optional: Boolean = false) =
		CommandArgument(name, CommandParam.TARGET, optional, TargetParser)

	fun string(name: String, optional: Boolean = false) = CommandArgument(name, CommandParam.STRING, optional)

	fun enum(name: String, vararg values: String, optional: Boolean = false): CommandArgument {
		return CommandArgument(name, optional, *values)
	}

	fun gameMode(name: String, optional: Boolean = false) = this.minecraftEnum(name, "GameMode", optional)

	fun minecraftEnum(name: String, enumType: String, optional: Boolean): CommandArgument {
		return CommandArgument(
			name, CommandParam.TEXT, optional, EnumParser,
			CommandEnumData(enumType, Array(0) { "" }, false)
		)
	}
}
