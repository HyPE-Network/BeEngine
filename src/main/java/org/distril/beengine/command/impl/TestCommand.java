package org.distril.beengine.command.impl;

import lombok.extern.log4j.Log4j2;
import org.distril.beengine.command.Command;
import org.distril.beengine.command.CommandSender;
import org.distril.beengine.command.data.Args;
import org.distril.beengine.server.Server;

@Log4j2
public class TestCommand extends Command {

	public TestCommand() {
		super("test", "Test command");
		this.setPermission("command.test");
	}

	@Override
	public void execute(CommandSender sender, Args args) {
		sender.sendMessage("Players size: " + Server.getInstance().getPlayers().size());
	}
}
