package org.distril.beengine;

import org.distril.beengine.server.Server;

public class Bootstrap {

	public static void main(String[] args) {
		var server = new Server();
		server.start();
	}
}
