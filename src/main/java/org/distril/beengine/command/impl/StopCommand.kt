package org.distril.beengine.command.impl

import org.distril.beengine.command.Command
import org.distril.beengine.command.CommandSender
import org.distril.beengine.command.data.Args
import org.distril.beengine.server.Server

class StopCommand : Command("stop", "Stopping server.", "command.stop") {

	override fun execute(sender: CommandSender, args: Args) {
		if (sender.hasPermission(this.permission)) Server.shutdown()
	}
}
