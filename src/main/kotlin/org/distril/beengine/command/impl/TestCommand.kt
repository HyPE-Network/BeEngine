package org.distril.beengine.command.impl

import org.distril.beengine.command.Command
import org.distril.beengine.command.CommandSender
import org.distril.beengine.command.data.Args
import org.distril.beengine.player.Player

class TestCommand : Command("test", "Test command") {

	init {
		this.addArguments {
			Float("health", true)
		}
	}

	override fun execute(sender: CommandSender, args: Args) {
		val player = sender as Player

		player.health = args.getFloat("health")!!

		player.sendMessage("Done: " + player.health)
	}
}
