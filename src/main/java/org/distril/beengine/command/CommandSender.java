package org.distril.beengine.command;

public interface CommandSender {

	void sendMessage(String message);

	boolean hasPermission(String permission);

	default boolean isConsole() {
		return false;
	}

	String getName();
}
