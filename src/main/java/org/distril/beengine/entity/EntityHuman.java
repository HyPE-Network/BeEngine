package org.distril.beengine.entity;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.GameType;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin;
import com.nukkitx.protocol.bedrock.packet.AddPlayerPacket;
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket;
import com.nukkitx.protocol.bedrock.packet.PlayerSkinPacket;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.network.data.Device;
import org.distril.beengine.player.Player;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Getter
public class EntityHuman extends Entity {

	private String username;
	@Setter
	private String xuid;
	private UUID uuid;
	private SerializedSkin skin;
	@Setter
	private Device device;

	public EntityHuman() {
		super(EntityType.HUMAN);
	}

	public void setUsername(String username) {
		this.username = username;
		this.uuid = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));
	}

	public void setSkin(SerializedSkin skin) {
		this.skin = skin;

		var packet = new PlayerSkinPacket();
		packet.setNewSkinName("");
		packet.setOldSkinName("");
		packet.setUuid(this.uuid);
		packet.setSkin(skin);
		packet.setTrustedSkin(true);

		this.getViewers().forEach(viewer -> viewer.sendPacket(packet));

		if (this instanceof Player player) {
			player.sendPacket(packet);
		}
	}

	public PlayerListPacket.Entry getPlayerListEntry() {
		var entry = new PlayerListPacket.Entry(this.uuid);
		entry.setEntityId(this.getId());
		entry.setName(this.username);
		entry.setSkin(this.skin);
		entry.setXuid(this.xuid);
		entry.setPlatformChatId("");
		entry.setBuildPlatform(this.getDevice().getDeviceOS());
		return entry;
	}

	@Override
	protected AddPlayerPacket createSpawnPacket(Player player) {
		var packet = new AddPlayerPacket();
		packet.setUuid(this.uuid);
		packet.setUsername(this.getUsername());
		packet.setRuntimeEntityId(this.getId());
		packet.setUniqueEntityId(this.getId());
		packet.setPosition(this.getPosition());
		packet.setGameType(GameType.SURVIVAL);
		packet.setMotion(Vector3f.ZERO);
		packet.setRotation(Vector3f.from(this.getPitch(), this.getYaw(), this.getHeadYaw()));
		packet.setDeviceId("");
		packet.setPlatformChatId("");
		packet.setHand(ItemData.AIR);
		return packet;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		EntityHuman that = (EntityHuman) obj;
		return Objects.equals(this.uuid, that.getUuid());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uuid);
	}
}
