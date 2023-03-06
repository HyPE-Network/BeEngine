package org.distril.beengine.util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class Utils {

	public static void createDirectories(String first, String... more) {
		try {
			var dir = Path.of(first, more);
			if (!Files.exists(dir)) {
				Files.createDirectories(dir);
			}
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
