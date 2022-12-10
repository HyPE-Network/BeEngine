package org.distril.beengine.player;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.AuthoritativeMovementMode;
import com.nukkitx.protocol.bedrock.data.SyncedPlayerMovementSettings;
import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket;
import com.nukkitx.protocol.bedrock.packet.SetPlayerGameTypePacket;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.network.data.Device;
import org.distril.beengine.network.data.LoginData;
import org.distril.beengine.player.data.Gamemode;
import org.distril.beengine.player.data.PlayerData;
import org.distril.beengine.player.data.provider.PlayerList;
import org.distril.beengine.server.Server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Log4j2
public class Player {

	private final Server server;
	private final BedrockServerSession session;
	private final LoginData loginData;
	private final Device device;
	private final String xuid;
	private final UUID uuid;
	private final String username;
	private final SerializedSkin skin;

	private final PlayerList playerList = new PlayerList(this);
	private PlayerData data;

	public Player(Server server, BedrockServerSession session, LoginData loginData) {
		this.server = server;
		this.session = session;
		this.loginData = loginData;

		this.device = loginData.getDevice();
		this.xuid = loginData.getXuid();
		this.uuid = loginData.getUuid();
		this.username = loginData.getUsername();
		this.skin = loginData.getSkin();
	}

	public void initialize() {
		this.server.getScheduler().prepareTask(() -> {
			try {
				this.data = this.server.getPlayerDataProvider().load(this.uuid);

				if (this.data == null) {
					this.data = new PlayerData();
					this.data.setGamemode(Gamemode.SURVIVAL);
				}

				this.completePlayerInitialization();
			} catch (IOException exception) {
				log.error("Failed to retrieve data of " + this.uuid, exception);
				this.disconnect();
			}
		}).async().schedule();
	}

	private void completePlayerInitialization() {
		this.server.getScheduler().prepareTask(() -> {
			if (this.server.getPlayers().size() >= this.server.getSettings().getMaximumPlayers()) {
				var packet = new PlayStatusPacket();
				packet.setStatus(PlayStatusPacket.Status.FAILED_SERVER_FULL_SUB_CLIENT);
				this.session.sendPacket(packet);
				return;
			}

			// Disconnect the player's other session if they're logged in on another device
			for (Player player : this.server.getPlayers()) {
				if (player.getUuid().equals(this.uuid)) {
					player.disconnect();
				}
			}

			this.setGamemode(this.data.getGamemode());

			var movementSettings = new SyncedPlayerMovementSettings();
			movementSettings.setMovementMode(AuthoritativeMovementMode.SERVER_WITH_REWIND);
			movementSettings.setRewindHistorySize(100);
			movementSettings.setServerAuthoritativeBlockBreaking(true);




			// Sent the full player list to this player
			Set<PlayerListPacket.Entry> entries = new HashSet<>();
			for (Player player : this.server.getPlayers()) {
				entries.add(player.getPlayerListEntry());
			}

			this.playerList.addEntries(entries);

			PlayStatusPacket playStatusPacket = new PlayStatusPacket();
			playStatusPacket.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
			this.sendPacket(playStatusPacket);

			log.info("done");
		}).schedule();
	}

	public void sendPacket(BedrockPacket packet) {
		if (!this.session.isClosed()) {
			this.session.sendPacket(packet);
		}
	}


	public void setGamemode(Gamemode gamemode) {
		var currentGamemode = this.data.getGamemode();
		if (gamemode != currentGamemode) {

			this.data.setGamemode(gamemode);

			var packet = new SetPlayerGameTypePacket();
			packet.setGamemode(gamemode.ordinal());
			this.sendPacket(packet);
		}
	}

	public PlayerListPacket.Entry getPlayerListEntry() {
		var entry = new PlayerListPacket.Entry(this.uuid);
		entry.setXuid(this.xuid);
		entry.setXuid(this.xuid);
		entry.setName(this.username);
		entry.setEntityId(1);
		entry.setBuildPlatform(this.device.getDeviceOS());
		entry.setSkin(this.skin);
		return entry;
	}


	public void disconnect() {
		this.session.disconnect();
	}
}
