package org.distril.beengine.material.item;

import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.defaults.ItemAir;
import org.distril.beengine.material.item.defaults.ItemNoop;

import java.util.EnumMap;
import java.util.Map;

public class ItemRegistry {

	private final Map<Material, Item> BY_MATERIAL = new EnumMap<>(Material.class);

	public void init() {
		this.register(Material.AIR, new ItemAir());
	}

	public void register(Material material, Item item) {
		BY_MATERIAL.put(material, item);
	}

	@SuppressWarnings("unchecked")
	public <T extends Item> T from(Material material) {
		return (T) BY_MATERIAL.getOrDefault(material, new ItemNoop(material))/*.clone()*/;
	}
}
