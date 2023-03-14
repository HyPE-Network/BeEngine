package org.distril.beengine.terminal;

import lombok.RequiredArgsConstructor;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.distril.beengine.server.Server;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

public class Terminal extends Thread {

	public static final ConsoleSender SENDER = new ConsoleSender();

	private final TerminalUnsafe terminalUnsafe;

	public Terminal(Server server) {
		super("Terminal Thread");
		this.setDaemon(true);

		this.terminalUnsafe = new TerminalUnsafe(server);
	}

	@Override
	public void run() {
		this.terminalUnsafe.start();
	}

	@RequiredArgsConstructor
	private static class TerminalUnsafe extends SimpleTerminalConsole {

		private final Server server;

		@Override
		protected boolean isRunning() {
			return this.server.isRunning();
		}

		@Override
		protected void runCommand(String command) {
			this.server.dispatchCommand(SENDER, command);
		}

		@Override
		protected void shutdown() {
			this.server.shutdown();
		}

		@Override
		protected LineReader buildReader(LineReaderBuilder builder) {
			builder.appName("BeEngine");
			builder.option(LineReader.Option.HISTORY_BEEP, false);
			builder.option(LineReader.Option.HISTORY_IGNORE_DUPS, true);
			builder.option(LineReader.Option.HISTORY_IGNORE_SPACE, true);
			return super.buildReader(builder);
		}
	}
}
