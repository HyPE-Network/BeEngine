package org.distril.beengine.player;

import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.*;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.packet.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.command.CommandSender;
import org.distril.beengine.entity.impl.EntityHuman;
import org.distril.beengine.inventory.Inventory;
import org.distril.beengine.inventory.InventoryHolder;
import org.distril.beengine.inventory.impl.PlayerInventory;
import org.distril.beengine.material.item.ItemPalette;
import org.distril.beengine.network.data.LoginData;
import org.distril.beengine.player.data.GameMode;
import org.distril.beengine.player.data.PlayerData;
import org.distril.beengine.player.data.attribute.Attribute;
import org.distril.beengine.player.data.attribute.Attributes;
import org.distril.beengine.player.manager.PlayerChunkManager;
import org.distril.beengine.server.Server;
import org.distril.beengine.util.BedrockResourceLoader;
import org.distril.beengine.world.Dimension;
import org.distril.beengine.world.World;
import org.distril.beengine.world.generator.impl.FlatGenerator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Log4j2
public class Player extends EntityHuman implements InventoryHolder, CommandSender {

	private final Server server;
	private final BedrockServerSession session;
	private final LoginData loginData;

	private final Attributes attributes;

	private final PlayerInventory inventory;

	private final PlayerChunkManager chunkManager = new PlayerChunkManager(this);

	private final Set<String> permissions = new HashSet<>();
	private Inventory openedInventory;

	private World world;

	private PlayerData data;

	private boolean loggedIn;

	public Player(Server server, BedrockServerSession session, LoginData loginData) {
		this.server = server;
		this.session = session;
		this.loginData = loginData;

		this.attributes = new Attributes(this);
		this.inventory = new PlayerInventory(this);

		this.setUsername(loginData.getUsername());
		this.setXuid(loginData.getXuid());
		this.setUuid(loginData.getUuid());
		this.setSkin(loginData.getSkin());
		this.setDevice(loginData.getDevice());
	}

	@Override
	public void onUpdate(long currentTick) {
		if (!this.loggedIn) {
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
		this.server.getScheduler().prepareTask(() -> {
			try {
				this.data = this.server.getPlayerDataProvider().load(this.getUuidForData());

				if (this.data == null) {
					this.data = new PlayerData();
				}

				this.world = new World("test world", Dimension.OVERWORLD, new FlatGenerator());
				this.world.addEntity(this);

				this.completePlayerInitialization();
			} catch (IOException exception) {
				log.error("Failed to load data of " + this.getUuidForData(), exception);
				this.disconnect();
			}
		}).async().schedule();
	}

	private void completePlayerInitialization() {
		if (this.server.getPlayers().size() >= this.server.getSettings().getMaximumPlayers()) {
			var packet = new PlayStatusPacket();
			packet.setStatus(PlayStatusPacket.Status.FAILED_SERVER_FULL_SUB_CLIENT);
			this.session.sendPacket(packet);
			return;
		}

		// Disconnect the player's other session if they're logged in on another device
		for (Player player : this.server.getPlayers()) {
			if (player.getUuid().equals(this.getUuid()) || player.getXuid().equals(this.getXuid())) {
				player.disconnect();
			}
		}

		this.setPitch(this.data.getPitch());
		this.setYaw(this.data.getYaw());
		this.setHeadYaw(this.data.getYaw());
		this.setPosition(this.data.getPosition());

		this.setGameMode(this.data.getGameMode());

		var startGamePacket = new StartGamePacket();
		startGamePacket.setUniqueEntityId(this.getId());
		startGamePacket.setRuntimeEntityId(this.getId());
		startGamePacket.setPlayerGameType(this.data.getGameMode().getType());
		startGamePacket.setPlayerPosition(this.getPosition());
		startGamePacket.setRotation(Vector2f.from(this.getPitch(), this.getYaw()));
		startGamePacket.setSeed(-1L);
		startGamePacket.setDimensionId(0);
		startGamePacket.setTrustingPlayers(false);
		startGamePacket.setLevelGameType(GameType.SURVIVAL);
		startGamePacket.setDifficulty(1);
		startGamePacket.setDefaultSpawn(Vector3i.from(0, 60, 0));
		startGamePacket.setDayCycleStopTime(7000);
		startGamePacket.setLevelName(this.server.getSettings().getMotd());
		startGamePacket.setLevelId("");
		startGamePacket.setGeneratorId(1);
		startGamePacket.setDefaultPlayerPermission(PlayerPermission.MEMBER);
		startGamePacket.setServerChunkTickRange(8);
		// startGamePacket.setVanillaVersion(Network.CODEC.getMinecraftVersion());
		startGamePacket.setVanillaVersion("1.17.40");
		startGamePacket.setPremiumWorldTemplateId("");
		startGamePacket.setInventoriesServerAuthoritative(true);
		startGamePacket.getGamerules().add(new GameRuleData<>("showcoordinates", true));
		startGamePacket.setItemEntries(ItemPalette.getItemEntries());

		var movementSettings = new SyncedPlayerMovementSettings();
		movementSettings.setMovementMode(AuthoritativeMovementMode.CLIENT);
		movementSettings.setRewindHistorySize(0);
		movementSettings.setServerAuthoritativeBlockBreaking(false);

		startGamePacket.setPlayerMovementSettings(movementSettings);
		startGamePacket.setCommandsEnabled(true);
		startGamePacket.setMultiplayerGame(true);
		startGamePacket.setBroadcastingToLan(true);
		startGamePacket.setMultiplayerCorrelationId(UUID.randomUUID().toString());
		startGamePacket.setXblBroadcastMode(GamePublishSetting.PUBLIC);
		startGamePacket.setPlatformBroadcastMode(GamePublishSetting.PUBLIC);
		startGamePacket.setCurrentTick(this.server.getCurrentTick());
		startGamePacket.setServerEngine("");
		startGamePacket.setPlayerPropertyData(NbtMap.EMPTY);
		startGamePacket.setWorldTemplateId(new UUID(0, 0));
		startGamePacket.setWorldEditor(false);
		startGamePacket.setChatRestrictionLevel(ChatRestrictionLevel.NONE);
		this.sendPacket(startGamePacket);

		this.server.addPlayer(this);

		this.sendPacket(ItemPalette.getCreativeContentPacket());

		var biomeDefinitionPacket = new BiomeDefinitionListPacket();
		biomeDefinitionPacket.setDefinitions(BedrockResourceLoader.BIOME_DEFINITIONS);
		this.sendPacket(biomeDefinitionPacket);

		var availableEntityIdentifiersPacket = new AvailableEntityIdentifiersPacket();
		availableEntityIdentifiersPacket.setIdentifiers(BedrockResourceLoader.ENTITY_IDENTIFIERS);
		this.sendPacket(availableEntityIdentifiersPacket);

		this.loggedIn = true;

		log.info("{} logged in [X = {}, Y = {}, Z = {}]", this.getUsername(), this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ());
	}

	private void doFirstSpawn() {
		this.setSpawned(true);

		this.sendPacket(this.server.getCommandRegistry().createPacketFor(this));

		this.attributes.sendAttributes();

		this.inventory.sendSlots(this);

		PlayStatusPacket playStatusPacket = new PlayStatusPacket();
		playStatusPacket.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
		this.sendPacket(playStatusPacket);
	}

	public void sendPacket(BedrockPacket packet) {
		if (!this.session.isClosed()) {
			this.session.sendPacket(packet);
		}
	}

	public UUID getUuidForData() {
		return UUID.nameUUIDFromBytes(this.getUsername().getBytes(StandardCharsets.UTF_8));
	}

	public void setGameMode(GameMode gameMode) {
		var currentGamemode = this.data.getGameMode();
		if (gameMode != currentGamemode) {

			this.data.setGameMode(gameMode);

			var packet = new SetPlayerGameTypePacket();
			packet.setGamemode(gameMode.ordinal());
			this.sendPacket(packet);
		}
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

	public void onDisconnect() {
		if (this.isSpawned()) {
			this.despawnFromAll();

			this.server.getScheduler().prepareTask(() -> {
				try {
					this.server.getPlayerDataProvider().save(this.getUuidForData(), this.data);
				} catch (IOException exception) {
					log.error("Failed to save data of " + this.getUuidForData(), exception);
				}

				this.world.removeEntity(this);
			}).async().schedule();
		}

		log.info("{} player left the server", this.getUsername());
	}

	public void disconnect() {
		this.session.disconnect();
	}

	public void disconnect(String reason) {
		this.session.disconnect(reason);
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
	public PlayerInventory getInventory() {
		return this.inventory;
	}

	@Override
	protected AddPlayerPacket createSpawnPacket(Player player) {
		var packet = super.createSpawnPacket(player);
		packet.setGameType(this.data.getGameMode().getType());
		packet.setHand(ItemData.AIR);
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

	public boolean equals(CommandSender sender) {
		return sender.getName().equals(this.getName());
	}
}
