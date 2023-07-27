package org.distril.beengine.command.impl

import org.distril.beengine.command.Command
import org.distril.beengine.command.CommandSender
import org.distril.beengine.command.data.Args

class TestCommand : Command("test", "Test command") {

	override fun execute(sender: CommandSender, args: Args) {
		TODO()
	}
}
