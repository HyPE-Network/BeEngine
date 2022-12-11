package org.distril.beengine;

import org.distril.beengine.server.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Bootstrap {

	public static void main(String[] args) {
		try {
			Files.createDirectory(Path.of("players"));
		} catch (IOException e) {

		}
		var server = new Server();
		server.start();
	}
}
