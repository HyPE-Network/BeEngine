package org.distril.beengine.console.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.util.PerformanceSensitive;

import java.util.List;

@Plugin(name = "MinecraftFormattingPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys("minecraftFormatting")
@PerformanceSensitive("allocation")
public class MinecraftFormattingPatternConverter extends LogEventPatternConverter {

	private static final String ANSI_RESET = "\u001B[m";
	private static final String LOOKUP = "0123456789abcdefklor";

	private static final char COLOR_CHAR = '§';

	private static final String[] ANSI_CODES = new String[]{
			"\u001B[0;30m",    // Black §0
			"\u001B[0;34m",    // Dark Blue §1
			"\u001B[0;32m",    // Dark Green §2
			"\u001B[0;36m",    // Dark Aqua §3
			"\u001B[0;31m",    // Dark Red §4
			"\u001B[0;35m",    // Dark Purple §5
			"\u001B[0;33m",    // Gold §6
			"\u001B[0;37m",    // Gray §7
			"\u001B[0;30;1m",  // Dark Gray §8
			"\u001B[0;34;1m",  // Blue §9
			"\u001B[0;32;1m",  // Green §a
			"\u001B[0;36;1m",  // Aqua §b
			"\u001B[0;31;1m",  // Red §c
			"\u001B[0;35;1m",  // Light Purple §d
			"\u001B[0;33;1m",  // Yellow §e
			"\u001B[0;37;1m",  // White §f
			"\u001B[5m",       // Obfuscated §k
			"\u001B[21m",      // Bold §l
			"\u001B[3m",       // Italic §o
			ANSI_RESET,        // Reset §r
	};

	private final List<PatternFormatter> formatters;

	private MinecraftFormattingPatternConverter(List<PatternFormatter> formatters) {
		super("MinecraftFormatting", "minecraftformatting");
		this.formatters = formatters;
	}

	public static MinecraftFormattingPatternConverter newInstance(Configuration config, String[] options) {
		if (options.length < 1 || options.length > 2) {
			LOGGER.error("Incorrect number of options on FormattingCodesPatternConverter. Expected at least 1, max 2 received " + options.length);
			return null;
		}

		if (options[0] == null) {
			LOGGER.error("No pattern supplied on FormattingCodesPatternConverter");
			return null;
		}

		var parser = PatternLayout.createPatternParser(config);
		var formatters = parser.parse(options[0]);
		return new MinecraftFormattingPatternConverter(formatters);
	}

	@Override
	public void format(LogEvent event, StringBuilder toAppendTo) {
		var start = toAppendTo.length();
		this.formatters.forEach(formatter -> formatter.format(event, toAppendTo));

		if (toAppendTo.length() != start) {
			var content = toAppendTo.substring(start);
			this.format(content, toAppendTo, start);
		}
	}

	private void format(String text, StringBuilder result, int start) {
		var next = text.indexOf(COLOR_CHAR);
		var last = text.length() - 1;
		if (next != -1 && next != last) {
			result.setLength(start + next);
			var pos = next;
			while (next != -1 && next < last) {
				var format = LOOKUP.indexOf(text.charAt(next + 1));
				if (format != -1) {
					if (pos != next) {
						result.append(text, pos, next);
					}

					result.append(ANSI_CODES[format]);
					pos = next += 2;
				} else {
					next++;
				}

				next = text.indexOf(COLOR_CHAR, next);
			}

			result.append(text, pos, text.length()).append(ANSI_RESET);
		}
	}
}
