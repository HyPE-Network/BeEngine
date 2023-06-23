package org.distril.beengine.material.block

import org.distril.beengine.material.Material
import org.distril.beengine.material.block.impl.NoopBlock
import java.util.*

class BlockRegistry {

	private val byMaterial: MutableMap<Material, Block> = EnumMap(Material::class.java)

	fun init() {
		// todo
	}

	fun register(material: Material, block: Block) {
		if (material.isBlock) this.byMaterial[material] = block
	}

	fun getBlockFromState(state: BlockState): Block {
		val identifier = state.states.getString("name")
		val material = Material.fromIdentifier(identifier)!!
		return this.from(material, state)
	}

	@Suppress("UNCHECKED_CAST")
	fun <T : Block> from(material: Material, state: BlockState? = null): T {
		return (this.byMaterial.getOrDefault(material, NoopBlock(material)).clone() as T).apply {
			if (state != null) this.state = state
		}
	}
}
