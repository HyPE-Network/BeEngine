package org.distril.beengine.command.impl;

import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.command.Command;
import org.distril.beengine.command.CommandSender;
import org.distril.beengine.command.data.Args;
import org.distril.beengine.material.Material;
import org.distril.beengine.player.Player;

@Log4j2
public class TestCommand extends Command {

	public TestCommand() {
		super("test", "Test command");
		this.setPermission("command.test");
	}

	@Override
	public void execute(CommandSender sender, Args args) {
		Player player = (Player) sender;
		ChunkRadiusUpdatedPacket chunkRadiusUpdatedPacket = new ChunkRadiusUpdatedPacket();
		chunkRadiusUpdatedPacket.setRadius(12 >> 4);

		player.sendPacket(chunkRadiusUpdatedPacket);

		var packet = new NetworkChunkPublisherUpdatePacket();
		packet.setRadius(200);
		packet.setPosition(player.getPosition().toInt());

		player.sendPacket(packet);


		var chunk = player.getWorld().getChunkManager().getChunk(player.getPosition().getFloorX() >> 4,
				player.getPosition().getFloorZ() >> 4);

		chunk.setBlock(1, 50, 2, 0, Material.BEDROCK.getBlock());
		chunk.setBlock(1, 51, 2, 0, Material.BEDROCK.getBlock());
		chunk.setBlock(1, 52, 2, 0, Material.BEDROCK.getBlock());

		log.info(chunk.createPacket());
		player.sendPacket(chunk.createPacket());

		player.sendPacket(packet);

		log.info(chunk.toString());

		player.sendMessage("Done!");

		/*var chunk = new World("test world", Dimension.OVERWORLD, new FlatGenerator()).getChunkManager().getChunk(0, 0);

		chunk.setBlock(1, 50, 2, 0, Material.BEDROCK.getBlock());
		chunk.setBlock(1, 51, 2, 0, Material.BEDROCK.getBlock());
		chunk.setBlock(1, 52, 2, 0, Material.BEDROCK.getBlock());

		chunk.createPacket();*/
	}
}
