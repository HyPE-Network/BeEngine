package org.distril.beengine.console;

import lombok.extern.log4j.Log4j2;
import org.distril.beengine.server.Server;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

@Log4j2
public class Console extends Thread {

	private static final ConsoleSender SENDER = new ConsoleSender();

	private final Server server;

	public Console(Server server) {
		super("Console Thread");
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
					var command = reader.readLine().trim();
					if (!command.isEmpty()) {
						this.server.dispatchCommand(SENDER, command);
					}
				} catch (EndOfFileException ignored) {/**/}
			}
		} catch (UserInterruptException | IOException exception) {
			this.server.shutdown();
		}
	}
}
