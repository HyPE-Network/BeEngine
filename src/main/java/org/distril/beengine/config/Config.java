package org.distril.beengine.config;

import lombok.RequiredArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class Config {

	private static final Yaml YAML;

	static {
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		Representer representer = new Representer(dumperOptions);
		representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		YAML = new Yaml(representer, dumperOptions);
	}

	private final File file;
	private final ConfigSection section;

	@SuppressWarnings("unchecked")
	public static Config load(File file) {
		try (var inputStream = new FileInputStream(file)) {
			ConfigSection section = new ConfigSection(YAML.loadAs(inputStream, LinkedHashMap.class));

			return new Config(file, section);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public Map<String, Object> getAllMap() {
		return this.section.getAllMap();
	}


	public ConfigSection getAll() {
		return this.section.getAll();
	}

	public Object get(String key) {
		return this.section.get(key);
	}

	public void set(String key, Object value) {
		this.section.set(key, value);
	}

	public ConfigSection getSection(String key) {
		return this.section.getSection(key);
	}

	public ConfigSection getSections() {
		return this.getSections(null);
	}

	public ConfigSection getSections(String key) {
		return this.section.getSections(key);
	}

	public <T> T get(String key, Class<T> type) {
		return this.get(key, type, null);
	}

	public <T> T get(String key, Class<T> type, T defaultValue) {
		return this.section.get(key, type, defaultValue);
	}

	public boolean is(String key, Class<?> type) {
		return this.section.is(key, type);
	}

	public boolean contains(String key) {
		return this.contains(key, false);
	}

	public boolean contains(String key, boolean ignoreCase) {
		return this.section.contains(key, ignoreCase);
	}

	public void remove(String key) {
		this.section.remove(key);
	}

	public Set<String> getKeys() {
		return this.getKeys(true);
	}

	public Set<String> getKeys(boolean child) {
		return this.section.getKeys(child);
	}


	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void save() {
		if (!this.file.exists()) {
			this.file.mkdirs();
		}

		try (var writer = new FileWriter(this.file)) {
			YAML.dump(this.section, writer);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
