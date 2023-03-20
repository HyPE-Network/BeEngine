package org.distril.beengine.network;

import com.nukkitx.protocol.bedrock.*;
import com.nukkitx.protocol.bedrock.v567.Bedrock_v567patch;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.player.handler.LoginPacketHandler;
import org.distril.beengine.server.Server;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionException;

@Log4j2
public class Network implements BedrockServerEventHandler {

	public static final BedrockPacketCodec CODEC = Bedrock_v567patch.BEDROCK_V567PATCH;

	public static final BedrockPong PONG = new BedrockPong();

	static {
		PONG.setEdition("MCPE");
		PONG.setGameType("Survival");
		PONG.setProtocolVersion(CODEC.getProtocolVersion());
		PONG.setVersion(CODEC.getMinecraftVersion());
		PONG.setPlayerCount(0);
	}

	private final Server server;

	private final BedrockServer bedrockServer;

	public Network(Server server, String ip, int port) {
		this.server = server;

		var bindAddress = new InetSocketAddress(ip, port);
		this.bedrockServer = new BedrockServer(bindAddress, Runtime.getRuntime().availableProcessors());
		this.bedrockServer.setHandler(this);

		PONG.setMotd(server.getSettings().getMotd());
		PONG.setSubMotd(PONG.getMotd());
		PONG.setMaximumPlayerCount(server.getSettings().getMaximumPlayers());
		PONG.setIpv4Port(port);
		PONG.setIpv6Port(port);
	}

	public void start() throws Exception {
		try {
			this.bedrockServer.bind().join();

			var bindAddress = this.bedrockServer.getBindAddress();

			var ip = bindAddress.getHostString();
			var port = bindAddress.getPort();
			log.info("Server started on " + ip + ":" + port);
		} catch (CompletionException exception) {
			if (exception.getCause() instanceof Exception) {
				throw (Exception) exception.getCause();
			}

			throw exception;
		}
	}

	@Override
	public boolean onConnectionRequest(InetSocketAddress address) {
		return true;
	}

	@Override
	public BedrockPong onQuery(InetSocketAddress address) {
		return PONG;
	}

	@Override
	public void onSessionCreation(BedrockServerSession session) {
		session.setCompressionLevel(this.server.getSettings().getCompressionLevel());
		session.setLogging(false);
		session.setPacketCodec(CODEC);
		session.setPacketHandler(new LoginPacketHandler(session, this.server));
	}

	public void stop() {
		this.bedrockServer.close(true);
	}
}
