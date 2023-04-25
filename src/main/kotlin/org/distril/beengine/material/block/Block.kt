package org.distril.beengine.material.block

import com.nukkitx.math.vector.Vector3i
import org.distril.beengine.material.Material
import org.distril.beengine.util.Direction
import org.distril.beengine.world.World
import java.util.*

abstract class Block(val material: Material, state: BlockState?) : Cloneable, BlockBehaviors {

	var state = state ?: BlockPalette.getDefaultState(material)!!

	var world: World? = null
	var position: Vector3i? = null

	fun getSide(face: Direction, step: Int = 1) = this.world!!.getBlock(face.getOffset(this.position!!, step))

	public override fun clone(): Block {
		val clone = super.clone() as Block
		clone.state = this.state
		clone.world = this.world
		clone.position = this.position
		return clone
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Block) return false

		return this.material === other.material && this.state == other.state
	}

	override fun hashCode() = Objects.hash(this.material, this.state)
}
