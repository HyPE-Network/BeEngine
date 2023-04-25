package org.distril.beengine.material.block.impl

import org.distril.beengine.material.Material
import org.distril.beengine.material.block.Block
import org.distril.beengine.material.block.BlockState

class NoopBlock(material: Material, state: BlockState? = null) : Block(material, state)
