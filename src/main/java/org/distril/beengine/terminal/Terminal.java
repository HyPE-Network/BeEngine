package org.distril.beengine.terminal;

import org.distril.beengine.server.Server;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class Terminal extends Thread {

	public static final ConsoleSender SENDER = new ConsoleSender();

	private static final String PROMPT = "> ";

	private final Server server;

	public Terminal(Server server) {
		super("Terminal Thread");
		this.setDaemon(true);

		this.server = server;
	}

	@Override
	public void run() {
		try {
			var reader = LineReaderBuilder.builder()
					.terminal(TerminalBuilder.terminal())
					.option(LineReader.Option.HISTORY_BEEP, false)
					.option(LineReader.Option.HISTORY_IGNORE_DUPS, true)
					.option(LineReader.Option.HISTORY_IGNORE_SPACE, true)
					.appName("BeEngine")
					.build();

			reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
			reader.unsetOpt(LineReader.Option.INSERT_TAB);
			while (this.server.isRunning()) {
				try {
					var command = reader.readLine(PROMPT);
					if (!command.isEmpty()) {
						this.server.dispatchCommand(SENDER, command);
					}
				} catch (UserInterruptException exception) {
					// Handle Ctrl + C
					this.server.stop();
				}
			}
		} catch (UserInterruptException | IOException exception) {
			this.server.stop();
		}
	}
}
