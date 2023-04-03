package org.distril.beengine.terminal

import org.apache.logging.log4j.LogManager
import org.distril.beengine.command.CommandSender

class ConsoleSender : CommandSender {

	override val name: String = "CONSOLE"

	override val isConsole: Boolean = true

	override fun sendMessage(message: String) = log.info(message)

	override fun hasPermission(permission: String) = true

	companion object {

		private val log = LogManager.getLogger(ConsoleSender::class.java)
	}
}
