package org.distril.beengine.server

import com.nukkitx.protocol.bedrock.BedrockPacket
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.distril.beengine.command.CommandRegistry
import org.distril.beengine.command.CommandSender
import org.distril.beengine.material.block.BlockPalette
import org.distril.beengine.material.block.BlockRegistry
import org.distril.beengine.material.item.ItemPalette
import org.distril.beengine.material.item.ItemRegistry
import org.distril.beengine.network.Network
import org.distril.beengine.player.Player
import org.distril.beengine.player.data.provider.NBTPlayerDataProvider
import org.distril.beengine.player.data.provider.PlayerDataProvider
import org.distril.beengine.scheduler.Scheduler
import org.distril.beengine.terminal.Terminal
import org.distril.beengine.util.BedrockResourceLoader
import org.distril.beengine.util.Utils
import org.distril.beengine.world.WorldRegistry
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.system.exitProcess

object Server {

	private val log = LogManager.getLogger(Server::class.java)

	var isRunning = true
		private set

	val terminal = Terminal()

	var settings: ServerSettings
		private set

	val scheduler: Scheduler

	val network: Network

	lateinit var itemRegistry: ItemRegistry
		private set

	lateinit var blockRegistry: BlockRegistry
		private set

	val commandRegistry = CommandRegistry()
	val worldRegistry = WorldRegistry()

	val players: MutableSet<Player> = Collections.newSetFromMap(ConcurrentHashMap())

	var playerDataProvider: PlayerDataProvider = NBTPlayerDataProvider()

	var currentTick: Long = 0
		private set

	init {
		try {
			val serverFile = Path.of("settings.yml").toFile()
			if (!serverFile.exists()) Files.copy(Utils.getResource(serverFile.name), serverFile.toPath())

			this.settings = ServerSettings(serverFile.toPath())

			this.scheduler = Scheduler(this.settings.taskTimeout)
		} catch (exception: IOException) {
			throw RuntimeException(exception)
		}

		if (this.settings.isDebugEnabled) {
			val context = LogManager.getContext(false) as LoggerContext
			val configuration = context.configuration
			val loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
			loggerConfig.level = Level.DEBUG
			context.updateLoggers()
		}

		this.network = Network(this.settings.ip, this.settings.port)

		Utils.createDirectories("players")
	}

	fun start() {
		this.terminal.start()
		log.info("Starting server...")

		runBlocking {
			async { BlockPalette.init() }.await()
			async { ItemPalette.init() }.await()

			itemRegistry = ItemRegistry()
			blockRegistry = BlockRegistry()

			awaitAll(*mutableListOf(
				async { BedrockResourceLoader.init() },
				async { itemRegistry.init() },
				async { blockRegistry.init() },
				async { worldRegistry.init() }
			).toTypedArray())
		}

		try {
			this.network.start()
		} catch (exception: Exception) {
			throw RuntimeException(exception)
		}

		this.startTickLoopProcessor()
	}

	private fun startTickLoopProcessor() {
		var nextTick = System.currentTimeMillis()
		try {
			while (this.isRunning) {
				try {
					val tickTime = System.currentTimeMillis()
					val time = tickTime - nextTick
					if (time < -25) {
						try {
							Thread.sleep(max(5, -time - 25))
						} catch (exception: InterruptedException) {
							log.error("Server interrupted whilst sleeping", exception)
						}
					}

					if (tickTime - nextTick < -25) return

					this.currentTick++

					this.scheduler.processTick(this.currentTick)

					if (nextTick - tickTime < -1000) {
						nextTick = tickTime
					} else {
						nextTick += 50
					}

					val current = System.currentTimeMillis()
					if (nextTick - 0.1 > current) {
						val allocated = nextTick - current - 1
						if (allocated > 0) Thread.sleep(allocated, 900000)
					}
				} catch (exception: RuntimeException) {
					log.error("Error whilst ticking server", exception)
				}
			}
		} catch (throwable: Throwable) {
			log.fatal("Exception happened while ticking server", throwable)
		}
	}

	fun shutdown() {
		log.info("Stopping server...")
		this.settings.save()

		log.info("Cancel all tasks...")
		this.scheduler.cancelAllTasks()

		this.players.forEach { it.disconnect("Server stopped") }
		this.network.stop()

		log.info("Server stopped!")
		this.terminal.interrupt()

		exitProcess(0)
	}

	fun dispatchCommand(sender: CommandSender, commandLine: String) {
		if (!this.commandRegistry.handle(sender, commandLine)) {
			sender.sendMessage(
				"Unknown command: $commandLine. Please check that the command exists and that you have " +
						"permission to use it."
			)
		}
	}

	fun getPlayer(username: String): Player? {
		val username = username.trim()
		if (username.isEmpty()) return null

		return this.players.firstOrNull { it.username.equals(username, ignoreCase = true) }
	}

	fun addPlayer(player: Player) {
		this.players.add(player)
	}

	fun removePlayer(player: Player) {
		this.players.remove(player)
	}

	fun removeOnlinePlayer(player: Player) {
		val packet = PlayerListPacket()
		packet.action = PlayerListPacket.Action.REMOVE
		packet.entries.add(PlayerListPacket.Entry(player.uuid))

		this.broadcastPacket(packet = packet)
	}

	fun addOnlinePlayer(player: Player) {
		val entries = mutableListOf<PlayerListPacket.Entry>().apply {
			players.forEach { this.add(it.playerListEntry) }
		}

		this.updatePlayersList(entries, setOf(player))
		this.updatePlayersList(setOf(player.playerListEntry))
	}

	private fun updatePlayersList(
		entries: Collection<PlayerListPacket.Entry>,
		players: Collection<Player> = this.players
	) {
		val packet = PlayerListPacket()
		packet.action = PlayerListPacket.Action.ADD
		packet.entries.addAll(entries)

		players.forEach { it.sendPacket(packet) }
	}

	fun broadcastPackets(targets: Collection<Player> = this.players, packets: Collection<BedrockPacket>) {
		targets.forEach { target -> packets.forEach { target.sendPacket(it) } }
	}

	fun broadcastPacket(targets: Collection<Player> = this.players, packet: BedrockPacket) {
		targets.forEach { it.sendPacket(packet) }
	}
}
