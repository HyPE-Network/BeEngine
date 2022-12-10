package org.distril.beengine.network;

import com.nukkitx.protocol.bedrock.*;
import com.nukkitx.protocol.bedrock.v557.Bedrock_v557;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionException;

@Log4j2
public class Network implements BedrockServerEventHandler {

	private static final BedrockPacketCodec CODEC = Bedrock_v557.V557_CODEC;

	private static final BedrockPong PONG = new BedrockPong();

	static {
		PONG.setEdition("MCPE");
		PONG.setGameType("Survival");
		PONG.setMotd("test motd");
		PONG.setSubMotd("test sub-motd");
		PONG.setPlayerCount(1);
		PONG.setMaximumPlayerCount(200);
		PONG.setProtocolVersion(CODEC.getProtocolVersion());
		PONG.setVersion(CODEC.getMinecraftVersion());
	}

	private final InetSocketAddress address;
	private final BedrockServer bedrockServer;

	public Network(String ip, int port) {
		this.address = new InetSocketAddress(ip, port);
		this.bedrockServer = new BedrockServer(this.address, Runtime.getRuntime().availableProcessors());
		this.bedrockServer.setHandler(this);

		PONG.setIpv4Port(port);
		PONG.setIpv6Port(port);
	}

	public void start() throws Exception {
		try {
			this.bedrockServer.bind().join();
			log.info("Server started on " + this.address.getAddress() + ":" + this.address.getPort());
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
		session.setCompressionLevel(7);
		log.info("Session connected {}", session);
	}

	public void stop() {
		this.bedrockServer.close();
	}
}
