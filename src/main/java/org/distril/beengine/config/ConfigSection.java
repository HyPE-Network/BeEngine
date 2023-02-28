package org.distril.beengine.config;

import java.util.*;

@SuppressWarnings("unchecked")
public class ConfigSection extends LinkedHashMap<String, Object> {

	public ConfigSection() {
		super();
	}

	public ConfigSection(String key, Object value) {
		this();

		this.set(key, value);
	}

	public ConfigSection(LinkedHashMap<String, Object> data) {
		this();

		if (data == null || data.isEmpty()) {
			return;
		}

		data.forEach((key, value) -> {
			if (value instanceof LinkedHashMap<?, ?>) {
				super.put(key, new ConfigSection((LinkedHashMap<String, Object>) value));
			} else if (value instanceof List<?> val) {
				super.put(key, this.parseList(val));
			} else {
				super.put(key, value);
			}
		});
	}

	private List<Object> parseList(List<?> list) {
		List<Object> newList = new ArrayList<>();

		list.forEach(value -> {
			if (value instanceof LinkedHashMap<?, ?>) {
				newList.add(new ConfigSection((LinkedHashMap<String, Object>) value));
			} else {
				newList.add(value);
			}
		});

		return newList;
	}

	public Map<String, Object> getAllMap() {
		return new LinkedHashMap<>(this);
	}


	public ConfigSection getAll() {
		return new ConfigSection(this);
	}

	public void set(String key, Object value) {
		var keys = key.split("\\.", 2);
		key = keys[0];
		if (keys.length > 1) {
			ConfigSection childSection = new ConfigSection();
			if (this.containsKey(key) && super.get(key) instanceof ConfigSection) {
				childSection = (ConfigSection) super.get(key);
			}

			childSection.set(keys[1], value);

			super.put(key, childSection);
		} else {
			super.put(key, value);
		}
	}

	public ConfigSection getSection(String key) {
		return this.get(key, ConfigSection.class, new ConfigSection());
	}

	public ConfigSection getSections() {
		return this.getSections(null);
	}

	public ConfigSection getSections(String key) {
		ConfigSection sections = new ConfigSection();
		ConfigSection parent = key == null || key.isEmpty() ? this.getAll() : this.getSection(key);
		if (parent == null) {
			return sections;
		}

		parent.forEach((key1, value) -> {
			if (value instanceof ConfigSection) {
				sections.put(key1, value);
			}
		});

		return sections;
	}

	public <T> T get(String key, Class<T> type) {
		return this.get(key, type, null);
	}

	public <T> T get(String key, Class<T> type, T defaultValue) {
		if (key == null || key.isEmpty()) {
			return defaultValue;
		}

		if (super.containsKey(key)) {
			return type.cast(super.get(key));
		}

		var keys = key.split("\\.", 2);
		if (!super.containsKey(keys[0])) {
			return type.cast(defaultValue);
		}

		var value = super.get(keys[0]);
		if (value instanceof ConfigSection section) {
			return section.get(keys[1], type, defaultValue);
		}

		return type.cast(defaultValue);
	}

	public boolean is(String key, Class<?> type) {
		return type.isInstance(this.get(key));
	}

	public boolean contains(String key) {
		return this.contains(key, false);
	}

	public boolean contains(String key, boolean ignoreCase) {
		for (var targetKey : this.getKeys(true)) {
			if (ignoreCase && targetKey.equalsIgnoreCase(key)) {
				return true;
			} else if (targetKey.equals(key)) {
				return true;
			}
		}

		return false;
	}

	public void remove(String key) {
		if (key == null || key.isEmpty()) {
			return;
		}

		if (super.containsKey(key)) {
			super.remove(key);
		} else if (this.containsKey(".")) {
			var keys = key.split("\\.", 2);
			if (super.get(keys[0]) instanceof ConfigSection section) {
				section.remove(keys[1]);
			}
		}
	}

	public Set<String> getKeys() {
		return this.getKeys(true);
	}

	public Set<String> getKeys(boolean child) {
		Set<String> result = new LinkedHashSet<>();
		this.forEach((key, value) -> {
			result.add(key);
			if (value instanceof ConfigSection section) {
				if (child) {
					section.getKeys(true).forEach(childKey -> result.add(key + "." + childKey));
				}
			}
		});

		return result;
	}
}
