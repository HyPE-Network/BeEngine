package org.distril.beengine.command.impl;

import org.distril.beengine.command.Command;
import org.distril.beengine.command.CommandSender;
import org.distril.beengine.command.data.Args;
import org.distril.beengine.server.Server;

public class StopCommand extends Command {

	public StopCommand() {
		super("stop", "Stopping server.");
		this.setPermission("command.stop");
	}

	@Override
	public void execute(CommandSender sender, Args args) {
		if (sender.hasPermission(this.getPermission())) {
			Server.getInstance().stop();
		}
	}
}
