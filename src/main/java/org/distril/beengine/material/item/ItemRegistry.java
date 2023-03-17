package org.distril.beengine.material.item;

import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.impl.NoopItem;

import java.util.EnumMap;
import java.util.Map;

public class ItemRegistry {

	private final Map<Material, Item> byMaterial = new EnumMap<>(Material.class);

	public void init() {
		// todo
	}

	public void register(Material material, Item item) {
		this.byMaterial.put(material, item);
	}

	@SuppressWarnings("unchecked")
	public <T extends Item> T from(Material material) {
		return (T) this.byMaterial.getOrDefault(material, new NoopItem(material)).clone();
	}
}
