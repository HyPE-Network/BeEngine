package org.distril.beengine.material.item

import org.distril.beengine.material.Material
import org.distril.beengine.material.item.impl.NoopItem
import java.util.*

class ItemRegistry {

	private val byMaterial: MutableMap<Material, Item> = EnumMap(Material::class.java)

	fun init() {
		// todo
	}

	fun register(material: Material, item: Item) {
		this.byMaterial[material] = item
	}

	@Suppress("UNCHECKED_CAST")
	fun <T : Item> from(material: Material) = this.byMaterial.getOrDefault(material, NoopItem(material)).clone() as T
}
