package org.distril.beengine.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.console.Console;
import org.distril.beengine.network.Network;
import org.distril.beengine.player.Player;
import org.distril.beengine.player.data.provider.NBTPlayerDataProvider;
import org.distril.beengine.player.data.provider.PlayerDataProvider;
import org.distril.beengine.scheduler.Scheduler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Log4j2
public class Server {

	@Getter(AccessLevel.NONE)
	private final AtomicBoolean running = new AtomicBoolean(true);

	private final Console console;
	private final ServerSettings settings = new ServerSettings();
	private final Network network;

	private final Scheduler scheduler = new Scheduler();

	private final Set<Player> players = new HashSet<>();

	@Setter
	@Getter
	private PlayerDataProvider playerDataProvider = new NBTPlayerDataProvider();

	private long currentTick;

	public Server() {
		this.console = new Console(this);

		this.network = new Network(this, this.settings.getIp(), this.settings.getPort());
	}

	public void start() {
		this.console.start();

		log.info("Starting server...");

		try {
			this.network.start();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}

		log.info("Server started!");
		this.startTickLoopProcessor();
	}

	private void startTickLoopProcessor() {
		var nextTick = System.currentTimeMillis();
		try {
			while (this.isRunning()) {
				try {
					var tickTime = System.currentTimeMillis();

					var time = tickTime - nextTick;
					if (time < -25) {
						try {
							Thread.sleep(Math.max(5, -time - 25));
						} catch (InterruptedException exception) {
							log.error("Server interrupted whilst sleeping", exception);
						}
					}

					if ((tickTime - nextTick) < -25) {
						return;
					}

					this.currentTick++;

					this.scheduler.processTick(this.currentTick);

					if ((nextTick - tickTime) < -1000) {
						nextTick = tickTime;
					} else {
						nextTick += 50;
					}

					var current = System.currentTimeMillis();

					if (nextTick - 0.1 > current) {
						var allocated = nextTick - current - 1;

						if (allocated > 0) {
							Thread.sleep(allocated, 900000);
						}
					}
				} catch (RuntimeException exception) {
					log.error("Error whilst ticking server", exception);
				}
			}
		} catch (Throwable throwable) {
			log.fatal("Exception happened while ticking server", throwable);
		}
	}

	public void stop() {
		this.console.interrupt();
	}

	public void addPlayer(Player player) {
		this.players.add(player);
	}

	public void removePlayer(Player player) {
		this.players.remove(player);
	}

	public Set<Player> getPlayers() {
		return Collections.unmodifiableSet(this.players);
	}

	public boolean isRunning() {
		return this.running.get();
	}
}
