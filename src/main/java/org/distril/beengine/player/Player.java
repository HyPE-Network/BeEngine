package org.distril.beengine.player;

import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.*;
import com.nukkitx.protocol.bedrock.data.entity.EntityDataMap;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin;
import com.nukkitx.protocol.bedrock.packet.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.command.CommandSender;
import org.distril.beengine.entity.impl.EntityHuman;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.InventoryHolder;
import org.distril.beengine.inventory.impl.PlayerInventory;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.ItemPalette;
import org.distril.beengine.network.Network;
import org.distril.beengine.network.data.LoginData;
import org.distril.beengine.player.data.GameMode;
import org.distril.beengine.player.data.PlayerData;
import org.distril.beengine.player.data.attribute.Attribute;
import org.distril.beengine.player.data.attribute.Attributes;
import org.distril.beengine.player.manager.PlayerChunkManager;
import org.distril.beengine.server.Server;
import org.distril.beengine.util.BedrockResourceLoader;
import org.distril.beengine.util.ChunkUtils;
import org.distril.beengine.util.ItemUtils;
import org.distril.beengine.world.chunk.ChunkLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Log4j2
public class Player extends EntityHuman implements InventoryHolder, CommandSender, ChunkLoader {

	private final Server server;
	private final BedrockServerSession session;
	private final LoginData loginData;

	private final Attributes attributes;

	private final PlayerInventory inventory;

	private final PlayerChunkManager chunkManager = new PlayerChunkManager(this);

	private final Set<String> permissions = new HashSet<>();
	private Inventory openedInventory;

	private PlayerData data;

	private boolean loggedIn;

	public Player(Server server, BedrockServerSession session, LoginData loginData) {
		this.server = server;
		this.session = session;
		this.loginData = loginData;

		this.attributes = new Attributes(this);
		this.inventory = new PlayerInventory(this);

		this.inventory.addItem(Material.BEDROCK.getItem()); // For Tests

		this.setUsername(loginData.getUsername());
		this.setXuid(loginData.getXuid());
		this.setUuid(loginData.getUuid());
		this.setSkin(loginData.getSkin());
		this.setDevice(loginData.getDevice());
	}

	@Override
	public void onUpdate(long currentTick) {
		if (!this.isConnected() || !this.loggedIn) {
			return;
		}

		if (this.isSpawned()) {
			this.chunkManager.queueNewChunks();
		}

		this.chunkManager.sendQueued();

		if (this.chunkManager.getChunksSent() >= 46 && !this.isSpawned()) {
			this.doFirstSpawn();
		}
	}

	public void initialize() {
		if (this.server.getPlayers().size() >= this.server.getSettings().getMaximumPlayers()) {
			this.disconnect("disconnectionScreen.serverFull");
			return;
		}

		this.server.getPlayers().forEach(target -> {
			if (target.equals(this) && target.getName().equalsIgnoreCase(this.getName()) || target.getUuidForData().equals(this.getUuidForData())) {
				target.disconnect("disconnectionScreen.loggedinOtherLocation");
			}
		});

		try {
			this.data = this.server.getPlayerDataProvider().load(this.getUuidForData());

			// no packet set data methods
			this.setPitch(data.getPitch());
			this.setYaw(data.getYaw());
			this.setLocation(data.getLocation());

			this.spawn(this.getLocation());
		} catch (IOException exception) {
			log.error("Failed to load data of " + this.getUuidForData(), exception);
			this.disconnect("Invalid data");
		}
	}

	public void completePlayerInitialization() {
		var startGamePacket = new StartGamePacket();
		startGamePacket.setUniqueEntityId(this.getId());
		startGamePacket.setRuntimeEntityId(this.getId());
		startGamePacket.setPlayerGameType(this.data.getGameMode().getType());
		startGamePacket.setPlayerPosition(this.getPosition());
		startGamePacket.setRotation(Vector2f.from(this.getPitch(), this.getYaw()));
		startGamePacket.setSeed(-1L);
		startGamePacket.setDimensionId(0);
		startGamePacket.setGeneratorId(1);
		startGamePacket.setLevelGameType(GameType.SURVIVAL);
		startGamePacket.setDifficulty(1);
		startGamePacket.setTrustingPlayers(false);
		startGamePacket.setDefaultSpawn(Vector3i.from(0, 60, 0));
		startGamePacket.setDayCycleStopTime(7000);
		startGamePacket.setLevelName(Network.PONG.getMotd());
		startGamePacket.setLevelId(this.getWorld().getWorldName());
		startGamePacket.setDefaultPlayerPermission(PlayerPermission.MEMBER);
		startGamePacket.setServerChunkTickRange(4);
		// startGamePacket.setVanillaVersion(Network.CODEC.getMinecraftVersion());
		startGamePacket.setVanillaVersion("1.17.40");
		startGamePacket.setPremiumWorldTemplateId("");
		startGamePacket.setInventoriesServerAuthoritative(true);
		startGamePacket.setItemEntries(ItemPalette.getItemEntries());

		var movementSettings = new SyncedPlayerMovementSettings();
		movementSettings.setMovementMode(AuthoritativeMovementMode.CLIENT);

		startGamePacket.setPlayerMovementSettings(movementSettings);
		startGamePacket.setCommandsEnabled(true);
		startGamePacket.setMultiplayerGame(true);
		startGamePacket.setBroadcastingToLan(true);
		startGamePacket.setMultiplayerCorrelationId(UUID.randomUUID().toString());
		startGamePacket.setXblBroadcastMode(GamePublishSetting.PUBLIC);
		startGamePacket.setPlatformBroadcastMode(GamePublishSetting.PUBLIC);
		startGamePacket.setCurrentTick(this.server.getCurrentTick());
		startGamePacket.setServerEngine("BeEngine");
		startGamePacket.setPlayerPropertyData(NbtMap.EMPTY);
		startGamePacket.setWorldTemplateId(new UUID(0, 0));
		startGamePacket.setWorldEditor(false);
		startGamePacket.setChatRestrictionLevel(ChatRestrictionLevel.NONE);
		this.sendPacket(startGamePacket);

		var biomePacket = new BiomeDefinitionListPacket();
		biomePacket.setDefinitions(BedrockResourceLoader.BIOME_DEFINITIONS);
		this.sendPacket(biomePacket);

		var entityPacket = new AvailableEntityIdentifiersPacket();
		entityPacket.setIdentifiers(BedrockResourceLoader.ENTITY_IDENTIFIERS);
		this.sendPacket(entityPacket);

		this.loggedIn = true;

		this.attributes.sendAll();

		this.server.addPlayer(this);
		this.server.addOnlinePlayer(this);
	}

	private void doFirstSpawn() {
		this.setSpawned(true);

		this.sendData(this);

		this.inventory.sendSlots(this);

		var packet = new PlayStatusPacket();
		packet.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
		this.sendPacket(packet);

		this.sendPacket(this.server.getCommandRegistry().createPacketFor(this));
		this.sendPacket(ItemPalette.getCreativeContentPacket());

		// packet set data methods
		this.setGameMode(this.data.getGameMode());

		this.chunkManager.getLoadedChunks().forEach(chunkKey -> {
			var chunkX = ChunkUtils.fromKeyX(chunkKey);
			var chunkZ = ChunkUtils.fromKeyZ(chunkKey);

			this.getWorld().getLoadedChunkEntities(chunkX, chunkZ).forEach(target -> {
				if (!this.equals(target) && target.isSpawned() && target.isAlive()) {
					target.spawnTo(this);
				}
			});
		});

		var position = this.getPosition();
		var realAddress = this.session.getRealAddress();
		log.info("{}[{}:{}] logged in with entity id {} at ({}, {}, {}, {})",
				this.getName(), realAddress.getHostName(), realAddress.getPort(), this.getId(),
				this.getWorld().getWorldName(), position.getX(), position.getY(), position.getZ());
	}

	public void sendPacket(BedrockPacket packet) {
		if (!this.session.isClosed()) {
			this.session.sendPacket(packet);
		}
	}

	public void sendPacketImmediately(BedrockPacket packet) {
		if (!this.session.isClosed()) {
			this.session.sendPacketImmediately(packet);
		}
	}

	public UUID getUuidForData() {
		return UUID.nameUUIDFromBytes(this.getUsername().getBytes(StandardCharsets.UTF_8));
	}

	public void setGameMode(GameMode gameMode) {
		this.data.setGameMode(gameMode);

		var packet = new SetPlayerGameTypePacket();
		packet.setGamemode(gameMode.ordinal());
		this.sendPacket(packet);
	}

	public boolean isSurvival() {
		return this.data.getGameMode() == GameMode.SURVIVAL;
	}

	public boolean isCreative() {
		return this.data.getGameMode() == GameMode.CREATIVE;
	}

	public boolean isAdventure() {
		return this.data.getGameMode() == GameMode.ADVENTURE;
	}

	public boolean isSpectator() {
		return this.data.getGameMode() == GameMode.SPECTATOR;
	}

	public void sendAttribute(Attribute attribute) {
		var packet = new UpdateAttributesPacket();
		packet.setRuntimeEntityId(this.getId());
		packet.getAttributes().add(attribute.toNetwork());
		this.sendPacket(packet);
	}

	public boolean isConnected() {
		return !this.session.isClosed();
	}

	public void disconnect() {
		this.disconnect("", true);
	}

	public void disconnect(String reason) {
		this.disconnect(reason, true);
	}

	public void disconnect(String reason, boolean showReason) {
		if (showReason && !reason.isEmpty()) {
			var packet = new DisconnectPacket();
			packet.setKickMessage(reason);
			this.sendPacketImmediately(packet);
		}

		if (this.loggedIn) {
			this.save();
		}

		this.closeOpenedInventory();

		this.chunkManager.getLoadedChunks().forEach(chunkKey -> {
			var chunkX = ChunkUtils.fromKeyX(chunkKey);
			var chunkZ = ChunkUtils.fromKeyZ(chunkKey);

			this.getWorld().getLoadedChunkEntities(chunkX, chunkZ).forEach(target -> {
				if (!target.equals(this)) {
					target.despawnFrom(this);
				}
			});
		});

		super.close();

		if (!this.session.isClosed()) {
			this.session.disconnect(showReason ? reason : "");
		}

		if (this.loggedIn) {
			this.server.removeOnlinePlayer(this);
		}

		this.loggedIn = false;

		this.chunkManager.clear();

		this.server.removePlayer(this);

		log.info("{} logged out due to {}", this.getName(), reason);
	}

	public void save() {
		this.save(false);
	}

	public void save(boolean async) {
		if (!this.isSpawned()) {
			throw new IllegalStateException("Tried to save closed player: " + this.getName());
		}

		if (this.loggedIn && !this.getName().isEmpty()) {
			this.server.getScheduler().prepareTask(() ->
					this.server.getPlayerDataProvider().save(this.getUuidForData(), this.data)
			).async(async).schedule();
		}
	}

	public void openInventory(Inventory inventory) {
		this.closeOpenedInventory();

		if (inventory.openFor(this)) {
			this.openedInventory = inventory;
		}
	}

	public void closeOpenedInventory() {
		if (this.openedInventory != null && this.openedInventory.closeFor(this)) {
			this.openedInventory = null;
		}
	}

	@Override
	public void setSkin(SerializedSkin skin) {
		super.setSkin(skin);

		var packet = new PlayerSkinPacket();
		packet.setNewSkinName("");
		packet.setOldSkinName("");
		packet.setUuid(this.getUuid());
		packet.setSkin(skin);
		packet.setTrustedSkin(true);

		this.sendPacket(packet);
	}

	@Override
	protected void onDataChange(EntityDataMap changeSet) {
		super.onDataChange(changeSet);

		var packet = new SetEntityDataPacket();
		packet.setRuntimeEntityId(this.getId());
		packet.getMetadata().putAll(changeSet);
		this.sendPacket(packet);
	}

	@Override
	public PlayerInventory getInventory() {
		return this.inventory;
	}

	@Override
	protected AddPlayerPacket createSpawnPacket(Player player) {
		var packet = super.createSpawnPacket(player);
		packet.setGameType(this.data.getGameMode().getType());
		packet.setHand(ItemUtils.toNetwork(this.inventory.getItemInHand()));
		return packet;
	}

	@Override
	public void sendMessage(String message) {
		var packet = new TextPacket();
		packet.setType(TextPacket.Type.RAW);
		packet.setXuid(this.getXuid());
		packet.setMessage(message);
		packet.setNeedsTranslation(true);

		this.sendPacket(packet);
	}

	public void addPermission(String permission) {
		if (this.permissions.add(permission)) {
			this.sendPacket(this.server.getCommandRegistry().createPacketFor(this));
		}
	}

	public void removePermission(String permission) {
		if (this.permissions.remove(permission)) {
			this.sendPacket(this.server.getCommandRegistry().createPacketFor(this));
		}
	}

	@Override
	public boolean hasPermission(String permission) {
		return this.permissions.contains(permission);
	}

	@Override
	public String getName() {
		return this.getUsername();
	}

	@Override
	public void setPosition(Vector3f position) {
		var from = this.getChunk();
		var to = this.getWorld().getChunk(position.toInt());

		if (!from.equals(to)) {
			from.removeEntity(this);
			to.addEntity(this);
		}

		super.setPosition(position);
	}

	public void sendPosition(MovePlayerPacket.Mode mode) {
		var packet = new MovePlayerPacket();
		packet.setRuntimeEntityId(this.getId());
		packet.setPosition(this.getPosition().add(0, this.getEyeHeight(), 0));
		packet.setRotation(Vector3f.from(this.getPitch(), this.getYaw(), this.getYaw()));
		packet.setMode(mode);

		if (mode == MovePlayerPacket.Mode.TELEPORT) {
			packet.setTeleportationCause(MovePlayerPacket.TeleportationCause.BEHAVIOR);
		}

		this.sendPacket(packet);
	}

	public boolean canInteract(Vector3f position) {
		return this.canInteract(position, this.isCreative() ? 13 : 7);
	}

	public boolean canInteract(Vector3f position, double maxDistance) {
		return this.canInteract(position, maxDistance, 6D);
	}

	public boolean canInteract(Vector3f position, double maxDistance, double maxDiff) {
		if (this.getPosition().distanceSquared(position) > maxDistance * maxDistance) {
			return false;
		}

		var directionPlane = this.getDirectionPlane();
		var fromDirection = directionPlane.dot(this.getPosition().toVector2(true));
		var toDirection = directionPlane.dot(position.toVector2(true));
		return (toDirection - fromDirection) >= -maxDiff;
	}

	private Vector2f getDirectionPlane() {
		var plane = Math.toRadians(this.getYaw()) - Math.PI / 2;
		return Vector2f.from(-Math.cos(plane), -Math.sin(plane)).normalize();
	}

	public void setUsingItem(boolean value) {
		this.getMetadata().setFlag(EntityFlag.USING_ITEM, value);
	}
}
