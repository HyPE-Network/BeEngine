package org.distril.beengine.entity.impl;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.PlayerPermission;
import com.nukkitx.protocol.bedrock.data.command.CommandPermission;
import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin;
import com.nukkitx.protocol.bedrock.packet.AddPlayerPacket;
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket;
import com.nukkitx.protocol.bedrock.packet.PlayerSkinPacket;
import lombok.Getter;
import lombok.Setter;
import org.distril.beengine.entity.EntityCreature;
import org.distril.beengine.entity.EntityType;
import org.distril.beengine.network.data.Device;
import org.distril.beengine.player.Player;
import org.distril.beengine.world.util.Location;

import java.util.UUID;

@Getter
@Setter
public class EntityHuman extends EntityCreature {

	private String username;
	private String xuid;
	private UUID uuid;
	private SerializedSkin skin;
	private Device device;

	public EntityHuman(Location location) {
		super(EntityType.HUMAN, location);
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
		packet.setMotion(Vector3f.ZERO);
		packet.setRotation(Vector3f.from(this.getPitch(), this.getYaw(), this.getYaw()));
		packet.setDeviceId("");
		packet.setPlatformChatId("");
		packet.setCommandPermission(CommandPermission.OPERATOR);
		packet.setPlayerPermission(PlayerPermission.OPERATOR);
		return packet;
	}
}
