package org.distril.beengine.console;

import lombok.extern.log4j.Log4j2;
import org.distril.beengine.command.CommandSender;

@Log4j2
public class ConsoleSender implements CommandSender {

	@Override
	public void sendMessage(String message) {
		log.info(message);
	}

	@Override
	public boolean hasPermission(String permission) {
		return true;
	}

	@Override
	public boolean isConsole() {
		return true;
	}

	@Override
	public String getName() {
		return "CONSOLE";
	}
}
