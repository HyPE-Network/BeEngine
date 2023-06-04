package org.distril.beengine.command.data

import com.nukkitx.protocol.bedrock.data.command.CommandEnumData
import com.nukkitx.protocol.bedrock.data.command.CommandParam
import org.distril.beengine.command.parser.EnumParser
import org.distril.beengine.command.parser.TargetParser

class ArgumentsBuilder {

    val arguments = mutableListOf<CommandArgument>()

    fun Integer(name: String, optional: Boolean = false) {
        this.arguments.add(CommandArgument(name, CommandParam.INT, optional))
    }

    fun Float(name: String, optional: Boolean = false) {
        this.arguments.add(CommandArgument(name, CommandParam.FLOAT, optional))
    }

    fun Target(name: String, optional: Boolean = false) {
        this.arguments.add(CommandArgument(name, CommandParam.TARGET, optional, TargetParser))
    }

    fun String(name: String, optional: Boolean = false) {
        this.arguments.add(CommandArgument(name, CommandParam.STRING, optional))
    }

    fun Enum(name: String, vararg values: String, optional: Boolean = false) {
        this.arguments.add(CommandArgument(name, optional, *values))
    }

    fun GameMode(name: String, optional: Boolean = false) = MinecraftEnum(name, "GameMode", optional)

    fun MinecraftEnum(name: String, enumType: String, optional: Boolean) {
        this.arguments.add(
            CommandArgument(
                name, CommandParam.TEXT, optional, EnumParser(),
                CommandEnumData(enumType, Array(0) { "" }, false)
            )
        )
    }
}
