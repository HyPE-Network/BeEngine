package org.distril.beengine.command;

import com.nukkitx.protocol.bedrock.packet.AvailableCommandsPacket;
import org.distril.beengine.command.impl.GamemodeCommand;
import org.distril.beengine.command.parser.Parser;
import org.distril.beengine.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {

	private final Map<String, Command> commands = new HashMap<>();

	public CommandRegistry() {
		this.register(new GamemodeCommand());
	}

	public void register(Command command) {
		this.commands.put(command.getName(), command);
	}

	public Command getCommand(String nameOrAlias) {
		var command = this.commands.get(nameOrAlias);
		if (command == null) {
			for (Command target : this.commands.values()) {
				if (List.of(target.getAliases()).contains(nameOrAlias)) {
					command = target;
				}
			}
		}

		return command;
	}

	public boolean handle(CommandSender sender, String commandLine) {
		var parsedArgs = this.parseArguments(commandLine);
		if (parsedArgs.size() == 0) {
			return false;
		}

		String commandOrAlias = parsedArgs.remove(0).toLowerCase();
		var command = this.getCommand(commandOrAlias);

		if (command == null) {
			return false;
		}

		Map<String, Object> args = new HashMap<>();

		var parsersIterator = command.getParsers().iterator();

		while (parsersIterator.hasNext()) {
			var argsIterator = parsedArgs.iterator();
			for (Map.Entry<String, Parser> entry : parsersIterator.next().entrySet()) {
				Parser parser = entry.getValue();

				if (argsIterator.hasNext()) {
					var next = argsIterator.next();
					if (next == null) {
						break;
					}

					var result = parser.parse(sender, next);
					if (result == null) {
						break;
					}

					args.put(entry.getKey(), result);
				}
			}
		}

		command.execute(sender, args);
		return true;
	}

	private List<String> parseArguments(String cmdLine) {
		var sb = new StringBuilder(cmdLine);
		List<String> args = new ArrayList<>();
		boolean notQuoted = true;
		int start = 0;

		for (int i = 0; i < sb.length(); i++) {
			if (sb.charAt(i) == '\\') {
				sb.deleteCharAt(i);
				continue;
			}

			if (sb.charAt(i) == ' ' && notQuoted) {
				var arg = sb.substring(start, i);
				if (!arg.isEmpty()) {
					args.add(arg);
				}

				start = i + 1;
			} else if (sb.charAt(i) == '"') {
				sb.deleteCharAt(i);
				--i;

				notQuoted = !notQuoted;
			}
		}

		var arg = sb.substring(start);
		if (!arg.isEmpty()) {
			args.add(arg);
		}

		return args;
	}

	public AvailableCommandsPacket createPacketFor(Player player) {
		var packet = new AvailableCommandsPacket();
		var data = packet.getCommands();
		for (Command command : this.commands.values()) {
			if (player.hasPermission(command.getPermission())) {
				data.add(command.toNetwork());
			}
		}

		return packet;
	}
}
