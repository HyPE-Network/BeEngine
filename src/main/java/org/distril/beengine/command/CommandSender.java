package org.distril.beengine.command;

public interface CommandSender {

	void sendMessage(String message);

	boolean hasPermission(String permission);
}
