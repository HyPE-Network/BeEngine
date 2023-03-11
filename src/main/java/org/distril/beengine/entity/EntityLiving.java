package org.distril.beengine.entity;

import com.nukkitx.protocol.bedrock.data.entity.EntityEventType;
import com.nukkitx.protocol.bedrock.packet.EntityEventPacket;
import org.distril.beengine.server.Server;

public abstract class EntityLiving extends Entity {

	public EntityLiving(EntityType type) {
		super(type);
	}

	@Override
	public void setHealth(float health) {
		var wasAlive = this.isAlive();
		super.setHealth(health);
		if (this.isAlive() && !wasAlive) {
			var packet = new EntityEventPacket();
			packet.setRuntimeEntityId(this.getId());
			packet.setType(EntityEventType.RESPAWN);

			Server.getInstance().broadcastPacket(this.getViewers(), packet);
		}
	}

	@Override
	public void kill() {
		if (this.isAlive()) {
			super.kill();

			// TODO: drop all items
		}
	}
}
