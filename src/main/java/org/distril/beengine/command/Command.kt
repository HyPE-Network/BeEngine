package org.distril.beengine.command

import com.nukkitx.protocol.bedrock.data.command.CommandData
import com.nukkitx.protocol.bedrock.data.command.CommandEnumData
import com.nukkitx.protocol.bedrock.data.command.CommandParamData
import org.distril.beengine.command.data.Args
import org.distril.beengine.command.data.CommandArgument
import org.distril.beengine.command.parser.EnumParser
import org.distril.beengine.command.parser.Parser
import java.util.*

abstract class Command(
	val name: String,
	private val description: String,
	val permission: String = "",
	vararg aliases: String = arrayOf()
) {

	private val arguments = mutableListOf<Array<CommandArgument>>()
	val parsers: MutableList<Map<String, Parser>> = LinkedList()
	val aliases: Array<String>

	init {
		val result = mutableListOf(this.name)

		aliases.forEach { result.add(it) }

		this.aliases = result.toTypedArray()
	}

	abstract fun execute(sender: CommandSender, args: Args)

	fun addArguments(vararg arguments: CommandArgument) {
		this.arguments.add(arguments.asList().toTypedArray())

		val parsers: MutableMap<String, Parser> = LinkedHashMap()
		arguments.forEach {
			val parser = it.parser
			parser.optional = it.optional
			if (parser is EnumParser) parser.setValues(it.enumData!!)
			parsers[it.name] = parser
		}

		this.parsers.add(parsers)
	}

	fun toNetwork(): CommandData {
		val parametersData: Array<Array<CommandParamData>?> = arrayOfNulls(arguments.size)
		for (i in parametersData.indices) {
			val parameters = arguments[i]
			parametersData[i] = parameters.map { it.toNetwork() }.toTypedArray()
		}

		val aliases = CommandEnumData(name + "Aliases", this.aliases, false)
		return CommandData(name, description, emptyList(), 0, aliases, parametersData)
	}
}
