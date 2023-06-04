package org.distril.beengine.player

import com.nukkitx.math.vector.Vector2f
import com.nukkitx.math.vector.Vector3f
import com.nukkitx.math.vector.Vector3i
import com.nukkitx.nbt.NbtMap
import com.nukkitx.protocol.bedrock.BedrockPacket
import com.nukkitx.protocol.bedrock.BedrockServerSession
import com.nukkitx.protocol.bedrock.data.*
import com.nukkitx.protocol.bedrock.data.entity.EntityDataMap
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag
import com.nukkitx.protocol.bedrock.packet.*
import org.distril.beengine.command.CommandSender
import org.distril.beengine.entity.Entity
import org.distril.beengine.entity.impl.EntityHuman
import org.distril.beengine.inventory.Inventory
import org.distril.beengine.inventory.impl.PlayerInventory
import org.distril.beengine.material.Material
import org.distril.beengine.material.item.ItemPalette
import org.distril.beengine.material.item.ItemPalette.creativeContentPacket
import org.distril.beengine.network.Network
import org.distril.beengine.network.data.LoginData
import org.distril.beengine.player.data.GameMode
import org.distril.beengine.player.data.PlayerData
import org.distril.beengine.player.data.attribute.Attribute
import org.distril.beengine.player.data.attribute.Attributes
import org.distril.beengine.player.manager.PlayerChunkManager
import org.distril.beengine.server.Server
import org.distril.beengine.util.BedrockResourceLoader
import org.distril.beengine.util.ChunkUtils.decodeX
import org.distril.beengine.util.ChunkUtils.decodeZ
import org.distril.beengine.util.ItemUtils
import org.distril.beengine.util.Utils.getLogger
import org.distril.beengine.world.chunk.ChunkLoader
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class Player(
    val session: BedrockServerSession,
    val loginData: LoginData
) : EntityHuman(), CommandSender, ChunkLoader {

    override val name = this.loginData.username

    val attributes = Attributes(this)

    override val inventory = PlayerInventory(this)
    val chunkManager = PlayerChunkManager(this)
    val permissions = mutableSetOf<String>()

    override var skin = super.skin
        set(skin) {
            field = skin
            super.skin = skin

            val packet = PlayerSkinPacket()
            packet.newSkinName = ""
            packet.oldSkinName = ""
            packet.uuid = uuid
            packet.skin = skin
            packet.isTrustedSkin = true

            this.sendPacket(packet)
        }

    var openedInventory: Inventory? = null
        private set
    lateinit var data: PlayerData
        private set
    var isLoggedIn = false
        private set

    init {
        this.inventory.addItem(Material.BEDROCK.getItem()) // For Tests

        this.username = this.loginData.username
        this.xuid = this.loginData.xuid
        this.uuid = this.loginData.uuid
        this.skin = this.loginData.skin
        this.device = this.loginData.device
    }

    override fun onUpdate(currentTick: Long) {
        if (!this.isConnected || !this.isLoggedIn) return

        if (this.isSpawned) {
            this.chunkManager.queueNewChunks()
        }

        if (this.chunkManager.chunksSentCount >= 46 && !this.isSpawned) this.doFirstSpawn()
    }

    fun initialize() {
        if (Server.players.size >= Server.settings.maximumPlayers) {
            this.disconnect("disconnectionScreen.serverFull")
            return
        }

        Server.players.forEach {
            if (it == this && it.name.equals(name, ignoreCase = true) || it.uuidForData == this.uuidForData) {
                it.disconnect("disconnectionScreen.loggedinOtherLocation")
            }
        }

        Server.playerDataProvider.load(this.uuidForData) { data, exception ->
            if (exception != null) {
                log.error("Failed to load data for ${this.uuidForData}", exception)
                this.disconnect("Invalid data")
                return@load
            }

            this.data = data

            // no packet set data methods
            this.pitch = this.data.pitch
            this.yaw = this.data.yaw

            this.init(this.data.location)

            this.completePlayerInitialization()
        }
    }

    private fun completePlayerInitialization() {
        val startGamePacket = StartGamePacket()
        startGamePacket.uniqueEntityId = this.id
        startGamePacket.runtimeEntityId = startGamePacket.uniqueEntityId
        startGamePacket.playerGameType = this.data.gameMode.type
        startGamePacket.playerPosition = this.position
        startGamePacket.rotation = Vector2f.from(this.pitch, this.yaw)
        startGamePacket.seed = -1L
        startGamePacket.dimensionId = 0
        startGamePacket.generatorId = 1
        startGamePacket.levelGameType = GameType.SURVIVAL
        startGamePacket.difficulty = 1
        startGamePacket.isTrustingPlayers = false
        startGamePacket.defaultSpawn = Vector3i.from(0, 60, 0)
        startGamePacket.dayCycleStopTime = 7000
        startGamePacket.levelName = Network.PONG.motd
        startGamePacket.levelId = this.world.worldName
        startGamePacket.defaultPlayerPermission = PlayerPermission.MEMBER
        startGamePacket.serverChunkTickRange = 4
        // startGamePacket.setVanillaVersion(Network.CODEC.getMinecraftVersion());
        startGamePacket.vanillaVersion = "1.17.40"
        startGamePacket.premiumWorldTemplateId = ""
        startGamePacket.isInventoriesServerAuthoritative = true
        startGamePacket.itemEntries = ItemPalette.entries.toList()

        val movementSettings = SyncedPlayerMovementSettings()
        movementSettings.movementMode = AuthoritativeMovementMode.CLIENT
        startGamePacket.playerMovementSettings = movementSettings
        startGamePacket.isCommandsEnabled = true
        startGamePacket.isMultiplayerGame = true
        startGamePacket.isBroadcastingToLan = true
        startGamePacket.multiplayerCorrelationId = UUID.randomUUID().toString()
        startGamePacket.xblBroadcastMode = GamePublishSetting.PUBLIC
        startGamePacket.platformBroadcastMode = GamePublishSetting.PUBLIC
        startGamePacket.currentTick = Server.currentTick
        startGamePacket.serverEngine = "BeEngine"
        startGamePacket.playerPropertyData = NbtMap.EMPTY
        startGamePacket.worldTemplateId = UUID(0, 0)
        startGamePacket.isWorldEditor = false
        startGamePacket.chatRestrictionLevel = ChatRestrictionLevel.NONE
        this.sendPacket(startGamePacket)

        this.sendPacket(BedrockResourceLoader.biomeDefinitionListPacket)
        this.sendPacket(BedrockResourceLoader.availableEntityIdentifiersPacket)

        this.isLoggedIn = true
        this.attributes.sendAll()

        Server.addPlayer(this)
        Server.addOnlinePlayer(this)

        this.chunkManager.queueNewChunks()
    }

    private fun doFirstSpawn() {
        this.isSpawned = true

        this.sendData(this)
        this.inventory.sendSlots(this)

        val packet = PlayStatusPacket()
        packet.status = PlayStatusPacket.Status.PLAYER_SPAWN
        this.sendPacket(packet)

        this.sendPacket(Server.commandRegistry.createPacketFor(this))
        this.sendPacket(creativeContentPacket)

        // packet set data methods
        this.setGameMode(this.data.gameMode)

        this.chunkManager.loadedChunks.forEach { chunkKey ->
            val chunkX = decodeX(chunkKey)
            val chunkZ = decodeZ(chunkKey)
            this.world.getLoadedChunkEntities(chunkX, chunkZ).forEach {
                if (this != it && it.isSpawned && it.isAlive) it.spawnFor(this)
            }
        }

        val position = this.position
        log.info(
            "$name[${this.session.realAddress}] logged in with entity " +
                    "id $id at (${this.world.worldName}, ${position.x}, ${position.y}, ${position.z})"
        )
    }

    fun sendPacket(packet: BedrockPacket) {
        if (!this.session.isClosed) this.session.sendPacket(packet)
    }

    fun sendPacketImmediately(packet: BedrockPacket) {
        if (!this.session.isClosed) this.session.sendPacketImmediately(packet)
    }

    val uuidForData: UUID
        get() = UUID.nameUUIDFromBytes(this.name.toByteArray(StandardCharsets.UTF_8))

    fun setGameMode(gameMode: GameMode) {
        this.data.gameMode = gameMode

        val packet = SetPlayerGameTypePacket()
        packet.gamemode = gameMode.type.ordinal

        this.sendPacket(packet)
    }

    val isSurvival: Boolean
        get() = this.data.gameMode === GameMode.SURVIVAL
    val isCreative: Boolean
        get() = this.data.gameMode === GameMode.CREATIVE
    val isAdventure: Boolean
        get() = this.data.gameMode === GameMode.ADVENTURE
    val isSpectator: Boolean
        get() = this.data.gameMode === GameMode.SPECTATOR

    fun sendAttribute(attribute: Attribute) {
        val packet = UpdateAttributesPacket()
        packet.runtimeEntityId = this.id
        packet.attributes.add(attribute.toNetwork())

        this.sendPacket(packet)
    }

    val isConnected: Boolean
        get() = !this.session.isClosed

    fun disconnect(reason: String = "", showReason: Boolean = true) {
        if (showReason && reason.isNotEmpty()) {
            val packet = DisconnectPacket()
            packet.kickMessage = reason

            this.sendPacketImmediately(packet)
        }

        if (this.isLoggedIn) this.save()

        this.closeOpenedInventory()

        this.chunkManager.loadedChunks.forEach {
            val chunkX = decodeX(it)
            val chunkZ = decodeZ(it)
            this.world.getLoadedChunkEntities(chunkX, chunkZ).forEach { target: Entity ->
                if (target != this) target.despawnFor(this)
            }
        }

        super.close()

        if (!this.session.isClosed) this.session.disconnect(reason)

        if (this.isLoggedIn) Server.removeOnlinePlayer(this)

        this.isLoggedIn = false

        this.chunkManager.clear()

        Server.removePlayer(this)

        log.info("$name logged out due to $reason")
    }

    fun save(async: Boolean = false) {
        if (this.isLoggedIn && this.name.isNotEmpty()) {
            Server.scheduler.scheduleTask(async = async) {
                Server.playerDataProvider.save(this.uuidForData, this.data) {
                    it?.let {
                        log.error("Failed to save data for ${this.uuidForData}", it)
                    }
                }
            }
        }
    }

    fun openInventory(inventory: Inventory) {
        this.closeOpenedInventory()

        if (inventory.openFor(this)) this.openedInventory = inventory
    }

    fun closeOpenedInventory() {
        if (this.openedInventory != null && this.openedInventory!!.closeFor(this)) {
            this.openedInventory = null
        }
    }

    override fun onDataChange(dataMap: EntityDataMap) {
        super.onDataChange(dataMap)

        val packet = SetEntityDataPacket()
        packet.runtimeEntityId = this.id
        packet.metadata.putAll(dataMap)

        this.sendPacket(packet)
    }

    override fun createSpawnPacket(player: Player): AddPlayerPacket {
        val packet = super.createSpawnPacket(player)
        packet.gameType = this.data.gameMode.type
        packet.hand = ItemUtils.toNetwork(this.inventory.getItemInHand())
        return packet
    }

    override fun sendMessage(message: String) {
        val packet = TextPacket()
        packet.type = TextPacket.Type.RAW
        packet.xuid = this.xuid
        packet.message = message
        packet.isNeedsTranslation = true

        this.sendPacket(packet)
    }

    fun addPermission(permission: String) {
        if (this.permissions.add(permission)) {
            this.sendPacket(Server.commandRegistry.createPacketFor(this))
        }
    }

    fun removePermission(permission: String) {
        if (this.permissions.remove(permission)) {
            this.sendPacket(Server.commandRegistry.createPacketFor(this))
        }
    }

    override fun hasPermission(permission: String) = this.permissions.contains(permission)

    override var position: Vector3f = Vector3f.ZERO
        get() = super.position
        set(value) {
            val from = this.chunk
            val vector3i = position.toInt()
            val to = this.world.chunkManager.getChunk(vector3i.x shr 4, vector3i.z shr 4)
            if (from != to) {
                from.removeEntity(this)
                to.addEntity(this)
            }

            field = value
            super.position = value
        }

    fun canInteract(
        position: Vector3f,
        maxDistance: Double = if (this.isCreative) 13.0 else 7.0,
        maxDiff: Double = 6.0
    ): Boolean {
        if (this.position.distanceSquared(position) > maxDistance * maxDistance) return false

        val directionPlane = this.directionPlane
        val fromDirection = directionPlane.dot(this.position.toVector2(true))
        val toDirection = directionPlane.dot(position.toVector2(true))
        return toDirection - fromDirection >= -maxDiff
    }

    private val directionPlane: Vector2f
        get() {
            val plane = Math.toRadians(this.yaw.toDouble()) - Math.PI / 2
            return Vector2f.from(-cos(plane), -sin(plane)).normalize()
        }

    fun setUsingItem(value: Boolean) = this.metadata.setFlag(EntityFlag.USING_ITEM, value)

    companion object {

        private val log = Player.getLogger()
    }
}
