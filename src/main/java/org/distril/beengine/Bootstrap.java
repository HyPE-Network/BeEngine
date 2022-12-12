package org.distril.beengine;

import org.distril.beengine.server.Server;

import java.io.InputStream;

public class Bootstrap {

	public static void main(String[] args) {
		var server = new Server();
		server.start();
	}

	public static InputStream getResource(String name) {
		return Bootstrap.class.getClassLoader().getResourceAsStream(name);
	}
}
