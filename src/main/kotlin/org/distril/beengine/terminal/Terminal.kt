package org.distril.beengine.terminal

import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.distril.beengine.server.Server
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder

class Terminal : Thread("Terminal Thread") {

	private val terminalUnsafe = TerminalUnsafe()

	init {
		this.isDaemon = true
	}

	override fun run() = this.terminalUnsafe.start()

	private class TerminalUnsafe : SimpleTerminalConsole() {

		override fun isRunning() = Server.isRunning

		override fun runCommand(command: String) = Server.dispatchCommand(sender, command)

		override fun shutdown() = Server.shutdown()

		override fun buildReader(builder: LineReaderBuilder): LineReader {
			builder.appName("BeEngine")
			builder.option(LineReader.Option.HISTORY_BEEP, false)
			builder.option(LineReader.Option.HISTORY_IGNORE_DUPS, true)
			builder.option(LineReader.Option.HISTORY_IGNORE_SPACE, true)
			return super.buildReader(builder)
		}
	}

	companion object {

		val sender = ConsoleSender()
	}
}
