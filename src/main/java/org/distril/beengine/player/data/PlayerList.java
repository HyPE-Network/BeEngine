package org.distril.beengine.player.data;

import com.nukkitx.protocol.bedrock.packet.PlayerListPacket;
import lombok.RequiredArgsConstructor;
import org.distril.beengine.player.Player;

import java.util.*;

@RequiredArgsConstructor
public class PlayerList {

	private final Set<PlayerListPacket.Entry> entries = new LinkedHashSet<>();

	private final Player player;

	public void addEntry(PlayerListPacket.Entry entry) {
		if (!this.entries.contains(entry)) {
			this.entries.add(entry);
			this.sendPacket(PlayerListPacket.Action.ADD, Collections.singleton(entry));
		}
	}

	public void addEntries(Collection<PlayerListPacket.Entry> entries) {
		Set<PlayerListPacket.Entry> validEntries = new HashSet<>();
		for (var entry : entries) {
			if (!this.entries.contains(entry)) {
				validEntries.add(entry);
			}
		}

		if (validEntries.size() > 0) {
			this.entries.addAll(validEntries);
			this.sendPacket(PlayerListPacket.Action.ADD, validEntries);
		}
	}

	public void removeEntry(PlayerListPacket.Entry entry) {
		if (this.entries.contains(entry)) {
			this.entries.remove(entry);
			this.sendPacket(PlayerListPacket.Action.REMOVE, Collections.singleton(entry));
		}
	}

	public void removeEntries(Collection<PlayerListPacket.Entry> entries) {
		Set<PlayerListPacket.Entry> validEntries = new HashSet<>();
		for (var entry : entries) {
			if (this.entries.contains(entry)) {
				validEntries.add(entry);
			}
		}

		if (validEntries.size() > 0) {
			this.entries.removeAll(validEntries);
			this.sendPacket(PlayerListPacket.Action.REMOVE, validEntries);
		}
	}

	public Set<PlayerListPacket.Entry> getEntries() {
		return Collections.unmodifiableSet(this.entries);
	}

	private void sendPacket(PlayerListPacket.Action action, Collection<PlayerListPacket.Entry> entries) {
		var packet = new PlayerListPacket();
		packet.setAction(action);
		packet.getEntries().addAll(entries);
		this.player.sendPacket(packet);
	}
}
