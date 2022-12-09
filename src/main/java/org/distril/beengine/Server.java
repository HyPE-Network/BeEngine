package org.distril.beengine;

import lombok.extern.log4j.Log4j2;
import org.distril.beengine.console.Console;

import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class Server {

	private final AtomicBoolean started = new AtomicBoolean(true);

	private final Console console;

	public Server() {
		this.console = new Console(this);
	}

	public void start() {
		this.console.start();

		log.info("Starting server...");


		log.info("Server started!");

		while (true) {
			// todo loop
		}
	}

	public void stop() {
		this.console.interrupt();
	}

	public boolean isStarted() {
		return this.started.get();
	}
}
