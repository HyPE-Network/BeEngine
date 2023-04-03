package org.distril.beengine.command.impl

import org.distril.beengine.command.Command
import org.distril.beengine.command.CommandSender
import org.distril.beengine.command.data.Args
import org.distril.beengine.command.data.ArgumentType
import org.distril.beengine.player.Player

class GameModeCommand : Command(
	"gamemode",
	"Sets a player's game mode.",
	"command.gamemode",
	"gm"
) {

	init {
		this.addArguments(
			ArgumentType.gameMode("gameMode"),
			ArgumentType.target("player", true)
		)

		this.addArguments(
			ArgumentType.integer("gameMode"),
			ArgumentType.target("player", true)
		)
	}

	override fun execute(sender: CommandSender, args: Args) {
		if (!sender.hasPermission(this.permission)) return

		if (args.isEmpty()) {
			sender.sendMessage("Use: /gamemode <gameMode> [player]")
			return
		}

		val gameMode = args.getGameMode("gameMode")
		if (gameMode == null) {
			sender.sendMessage("Game mode '${args.getString("gameMode")}' is invalid")
			return
		}

		var target = args.getTarget("player")
		if (sender.isConsole && (!args.has("player") || target == null)) {
			sender.sendMessage("No targets matched selector")
			return
		}

		if (target == null) target = sender as Player

		if (target != sender) {
			sender.sendMessage("Set '${target.username}'s game mode to ${gameMode.identifier}")
		} else {
			sender.sendMessage("Set own game mode to ${gameMode.identifier}")
		}

		target.setGameMode(gameMode)
		target.sendMessage("Your game mode has been updated to ${gameMode.identifier}")
	}
}
