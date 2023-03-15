package org.distril.beengine.server;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.distril.beengine.Bootstrap;
import org.distril.beengine.command.CommandRegistry;
import org.distril.beengine.command.CommandSender;
import org.distril.beengine.console.Console;
import org.distril.beengine.material.item.ItemRegistry;
import org.distril.beengine.network.Network;
import org.distril.beengine.player.Player;
import org.distril.beengine.player.data.provider.NBTPlayerDataProvider;
import org.distril.beengine.player.data.provider.PlayerDataProvider;
import org.distril.beengine.scheduler.Scheduler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Log4j2
public class Server {

	@Getter
	private static Server instance;

	@Getter(AccessLevel.NONE)
	private final AtomicBoolean running = new AtomicBoolean(true);

	private final Terminal terminal;
	private final ServerSettings settings;
	private final Network network;

	private final Scheduler scheduler = new Scheduler();

	private final ItemRegistry itemRegistry = new ItemRegistry();
	private final BlockRegistry blockRegistry = new BlockRegistry();

	private final CommandRegistry commandRegistry = new CommandRegistry();

	private final WorldRegistry worldRegistry = new WorldRegistry();

	private final Set<Player> players = Collections.newSetFromMap(new ConcurrentHashMap<>());

	@Setter
	@Getter
	private PlayerDataProvider playerDataProvider = new NBTPlayerDataProvider();

	private long currentTick;

	public Server() {
		Server.instance = this;

		try {
			var serverFile = Path.of("settings.yml").toFile();
			if (!serverFile.exists()) {
				Files.copy(Bootstrap.getResource(serverFile.getName()), serverFile.toPath());
			}

			this.settings = new ServerSettings(serverFile.toPath());
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		if (this.settings.isDebugEnabled()) {
			var context = (LoggerContext) LogManager.getContext(false);
			var configuration = context.getConfiguration();

			var loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
			loggerConfig.setLevel(Level.DEBUG);

			context.updateLoggers();
		}

		this.terminal = new Terminal(this);

		this.network = new Network(this, this.settings.getIp(), this.settings.getPort());

		Utils.createDirectories("players");
	}

	public void start() {
		this.terminal.start();

		log.info("Starting server...");

		this.itemRegistry.init();
		this.blockRegistry.init();

		this.worldRegistry.init();

		try {
			this.network.start();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}

		log.info("Server started!");
		this.startTickLoopProcessor();
	}

	@SuppressWarnings("BusyWait")
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

	public void shutdown() {
		log.info("Stopping server...");
		this.settings.save();

		log.info("Cancel all tasks...");
		this.scheduler.cancelAllTasks();

		this.players.forEach(player -> player.disconnect("Server stopped"));

		this.network.stop();

		log.info("Server stopped!");
		this.terminal.interrupt();

		System.exit(0);
	}

	public void dispatchCommand(CommandSender sender, String commandLine) {
		if (sender != null) {
			if (!this.commandRegistry.handle(sender, commandLine)) {
				sender.sendMessage("Unknown command: " + commandLine + ". Please check that the command exists and that you have " +
						"permission to use it.");
			}
		}
	}

	public Player getPlayer(String username) {
		if (username == null) {
			return null;
		}

		username = username.trim();

		if (username.isEmpty()) {
			return null;
		}

		for (var target : this.players) {
			if (target.getUsername().equalsIgnoreCase(username)) {
				return target;
			}
		}

		return null;

	}

	public void addPlayer(Player player) {
		this.players.add(player);
	}

	public void removePlayer(Player player) {
		this.players.remove(player);
	}

	public void removeOnlinePlayer(Player player) {
		var packet = new PlayerListPacket();
		packet.setAction(PlayerListPacket.Action.REMOVE);
		packet.getEntries().add(new PlayerListPacket.Entry(player.getUuid()));

		this.broadcastPacket(this.players, packet);
	}

	public void addOnlinePlayer(Player player) {
		List<PlayerListPacket.Entry> entries = new ArrayList<>();

		this.players.forEach(target -> entries.add(target.getPlayerListEntry()));

		this.updatePlayersList(entries, Collections.singleton(player));

		this.updatePlayersList(Collections.singleton(player.getPlayerListEntry()), this.players);
	}

	private void updatePlayersList(Collection<PlayerListPacket.Entry> entries, Collection<Player> players) {
		var packet = new PlayerListPacket();
		packet.setAction(PlayerListPacket.Action.ADD);
		packet.getEntries().addAll(entries);

		players.forEach(player -> player.sendPacket(packet));
	}

	public Set<Player> getPlayers() {
		return Collections.unmodifiableSet(this.players);
	}

	public boolean isRunning() {
		return this.running.get();
	}

	public void broadcastPackets(Collection<Player> targets, Collection<? extends BedrockPacket> packets) {
		targets.forEach(target -> packets.forEach(target::sendPacket));
	}

	public void broadcastPacket(Collection<Player> targets, BedrockPacket packet) {
		targets.forEach(target -> target.sendPacket(packet));
	}
}
