package org.distril.beengine.network;

import com.nukkitx.protocol.bedrock.*;
import com.nukkitx.protocol.bedrock.v560.Bedrock_v560;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.player.handler.LoginPacketHandler;
import org.distril.beengine.server.Server;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionException;

@Log4j2
public class Network implements BedrockServerEventHandler {

	public static final BedrockPacketCodec CODEC = Bedrock_v560.V560_CODEC;

	private static final BedrockPong PONG = new BedrockPong();

	static {
		PONG.setEdition("MCPE");
		PONG.setGameType("Survival");
		PONG.setMotd("test motd");
		PONG.setSubMotd("test sub-motd");
		PONG.setPlayerCount(0);
		PONG.setProtocolVersion(CODEC.getProtocolVersion());
		PONG.setVersion(CODEC.getMinecraftVersion());
	}

	private final Server server;

	private final BedrockServer bedrockServer;

	public Network(Server server, String ip, int port) {
		this.server = server;

		InetSocketAddress bindAddress = new InetSocketAddress(ip, port);
		this.bedrockServer = new BedrockServer(bindAddress, Runtime.getRuntime().availableProcessors());
		this.bedrockServer.setHandler(this);

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
		session.setLogging(false);
		session.setPacketHandler(new LoginPacketHandler(session, this.server));
	}

	public void stop() {
		this.bedrockServer.close();
	}
}
