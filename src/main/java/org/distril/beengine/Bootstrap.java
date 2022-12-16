package org.distril.beengine;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.distril.beengine.server.Server;

import java.io.InputStream;

public class Bootstrap {

	public static void main(String[] args) {
		Bootstrap.enableDebug();
		var server = new Server();
		server.start();
	}

	public static InputStream getResource(String name) {
		return Bootstrap.class.getClassLoader().getResourceAsStream(name);
	}

	private static void enableDebug() {
		var context = (LoggerContext) LogManager.getContext(false);
		var configuration = context.getConfiguration();

		var loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		loggerConfig.setLevel(Level.DEBUG);

		context.updateLoggers();
	}
}
