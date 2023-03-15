package org.distril.beengine.command;

import com.nukkitx.protocol.bedrock.packet.AvailableCommandsPacket;
import org.distril.beengine.command.data.Args;
import org.distril.beengine.command.impl.GameModeCommand;
import org.distril.beengine.command.impl.StopCommand;
import org.distril.beengine.command.impl.TestCommand;
import org.distril.beengine.player.Player;

import java.util.*;

public class CommandRegistry {

	private final Map<String, Command> commands = new HashMap<>();

	public CommandRegistry() {
		this.register(new GameModeCommand());
		this.register(new StopCommand());
		this.register(new TestCommand());
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

		var commandOrAlias = parsedArgs.remove(0).toLowerCase();
		var command = this.getCommand(commandOrAlias);

		if (command == null) {
			return false;
		}

		Map<String, String> args = new HashMap<>();

		var parsersIterator = command.getParsers().iterator();

		while (parsersIterator.hasNext()) {
			var argsIterator = parsedArgs.iterator();

			for (var entry : parsersIterator.next().entrySet()) {
				var parser = entry.getValue();

				if (argsIterator.hasNext()) {
					var next = this.getNext(argsIterator);

					var result = parser.parse(sender, next);
					if (result == null) {
						break;
					}

					args.put(entry.getKey(), result);
				}
			}
		}

		command.execute(sender, new Args(args));
		return true;
	}

	private String getNext(Iterator<String> iterator) {
		var next = iterator.next();
		if (next.startsWith("\"")) {
			if (next.endsWith("\"")) {
				return next.substring(1, next.length() - 1);
			}

			StringBuilder nameBuilder = new StringBuilder(next.substring(1));
			while (iterator.hasNext()) {
				var current = iterator.next();
				if (current.endsWith("\"")) {
					nameBuilder.append(" ").append(current, 0, current.length() - 1);
					return nameBuilder.toString();
				}

				nameBuilder.append(" ").append(current);
			}

			return nameBuilder.toString();
		}

		return next;
	}

	private List<String> parseArguments(String commandArgs) {
		var sb = new StringBuilder(commandArgs);
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

		this.commands.values().forEach(command -> {
			if (player.hasPermission(command.getPermission())) {
				data.add(command.toNetwork());
			}
		});

		return packet;
	}
}
