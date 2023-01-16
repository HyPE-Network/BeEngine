package org.distril.beengine.command.impl;

import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.command.Command;
import org.distril.beengine.command.CommandSender;
import org.distril.beengine.command.data.Args;
import org.distril.beengine.player.Player;

@Log4j2
public class TestCommand extends Command {

	public TestCommand() {
		super("test", "Test command");
		this.setPermission("command.test");
	}

	@Override
	public void execute(CommandSender sender, Args args) {
		if (args.isEmpty()) {
			Player player = (Player) sender;
			ChunkRadiusUpdatedPacket chunkRadiusUpdatedPacket = new ChunkRadiusUpdatedPacket();
			chunkRadiusUpdatedPacket.setRadius(12 >> 4);

			player.sendPacket(chunkRadiusUpdatedPacket);


			var chunk = player.getWorld().getChunkManager().getChunk(player.getPosition().getFloorX() >> 4,
					player.getPosition().getFloorZ() >> 4);

			log.info(chunk.createPacket());
			player.sendPacket(chunk.createPacket());

			var packet = new NetworkChunkPublisherUpdatePacket();
			packet.setRadius(12);
			packet.setPosition(player.getPosition().toInt());

			player.sendPacket(packet);

			log.info(chunk.toString());

			player.sendMessage("Done!");
		}
	}
}
