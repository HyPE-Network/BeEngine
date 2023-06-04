package org.distril.beengine.terminal

import org.distril.beengine.command.CommandSender
import org.distril.beengine.util.Utils.getLogger

class ConsoleSender : CommandSender {

    override val name: String = "CONSOLE"

    override val isConsole: Boolean = true

    override fun sendMessage(message: String) = log.info(message)

    override fun hasPermission(permission: String) = true

    companion object {

        private val log = ConsoleSender.getLogger()
    }
}
