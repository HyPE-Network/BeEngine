package org.distril.beengine.command

import com.nukkitx.protocol.bedrock.packet.AvailableCommandsPacket
import org.distril.beengine.command.data.Args
import org.distril.beengine.command.impl.GameModeCommand
import org.distril.beengine.command.impl.StopCommand
import org.distril.beengine.command.impl.TestCommand
import org.distril.beengine.player.Player
import java.util.*

class CommandRegistry {

	private val commands = mutableMapOf<String, Command>()

	init {
		this.register(GameModeCommand())
		this.register(StopCommand())
		this.register(TestCommand())
	}

	fun register(command: Command) {
		this.commands[command.name] = command
	}

	fun getCommand(nameOrAlias: String): Command? {
		val command = this.commands[nameOrAlias]
		if (command == null) {
			this.commands.values.forEach { if (it.aliases.contains(nameOrAlias)) return it }
		}

		return command
	}

	fun handle(sender: CommandSender, commandLine: String): Boolean {
		val parsedArgs = this.parseArguments(commandLine).toMutableList()
		if (parsedArgs.isEmpty()) return false

		val commandOrAlias = parsedArgs.removeAt(0).lowercase(Locale.getDefault())
		val command = this.getCommand(commandOrAlias) ?: return false

		val args = mutableMapOf<String, String>()
		val parsersIterator = command.parsers.iterator()
		while (parsersIterator.hasNext()) {
			val argsIterator = parsedArgs.iterator()
			for ((key, parser) in parsersIterator.next()) {
				if (argsIterator.hasNext()) {
					val next = this.getNext(argsIterator)
					val result = parser.parse(sender, next) ?: break
					args[key] = result
				}
			}
		}

		command.execute(sender, Args(args))
		return true
	}

	private fun getNext(iterator: Iterator<String>): String {
		val next = iterator.next()
		if (next.startsWith("\"")) {
			if (next.endsWith("\"")) return next.substring(1, next.length - 1)

			val nameBuilder = StringBuilder(next.substring(1))
			while (iterator.hasNext()) {
				val current = iterator.next()
				if (current.endsWith("\"")) {
					nameBuilder.append(" ").append(current, 0, current.length - 1)
					return nameBuilder.toString()
				}

				nameBuilder.append(" ").append(current)
			}

			return nameBuilder.toString()
		}

		return next
	}

	private fun parseArguments(commandArgs: String): List<String> {
		val sb = StringBuilder(commandArgs)
		val args = mutableListOf<String>()
		var notQuoted = true
		var start = 0
		var i = 0
		while (i < sb.length) {
			if (sb[i] == '\\') {
				sb.deleteCharAt(i)
				i++
				continue
			}

			if (sb[i] == ' ' && notQuoted) {
				val arg = sb.substring(start, i)
				if (arg.isNotEmpty()) args.add(arg)

				start = i + 1
			} else if (sb[i] == '"') {
				sb.deleteCharAt(i)
				--i
				notQuoted = !notQuoted
			}

			i++
		}

		val arg = sb.substring(start)
		if (arg.isNotEmpty()) args.add(arg)

		return args
	}

	fun createPacketFor(player: Player): AvailableCommandsPacket {
		val packet = AvailableCommandsPacket()
		this.commands.values.forEach { command ->
			command.permission.let { if (it == "" || player.hasPermission(it)) packet.commands.add(command.toNetwork()) }
		}

		return packet
	}
}
