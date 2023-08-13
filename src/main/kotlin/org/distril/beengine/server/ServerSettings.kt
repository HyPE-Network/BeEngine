package org.distril.beengine.server

import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.IOException
import java.nio.file.Path

class ServerSettings(path: Path) {

	private val loader = YamlConfigurationLoader.builder().path(path).build()

	private val config by lazy {
		try {
			this.loader.load()
		} catch (exception: IOException) {
			throw RuntimeException("An error occurred while loading ServerSettings configuration:", exception)
		}
	}

	var motd = this.config.node("server", "motd").string!!
	var maximumPlayers = this.config.node("server", "max-players").int
	var isXboxRequired = this.config.node("server", "xbox-require").boolean
	var isEncryptionEnabled = this.config.node("server", "enable-encryption").boolean
	var isDebugEnabled = this.config.node("server", "debug").boolean

	var taskTimeout = this.config.node("scheduler", "timeout").long

	var ip = this.config.node("network", "ip").string!!
	var port = this.config.node("network", "port").int
	var compressionLevel = this.config.node("network", "compression-level").int

	var defaultWorldName = this.config.node("world", "default").string!!
	var chunkExpiryTime = this.config.node("world", "chunk", "expiry-time").int

	fun save() {
		try {
			this.loader.save(this.config)
		} catch (exception: ConfigurateException) {
			throw RuntimeException("Unable to save ServerSettings configuration: ${exception.message}")
		}
	}
}
