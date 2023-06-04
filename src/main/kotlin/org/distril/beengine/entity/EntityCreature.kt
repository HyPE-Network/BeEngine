package org.distril.beengine.entity

import org.distril.beengine.inventory.InventoryHolder
import org.distril.beengine.inventory.impl.CreatureInventory

abstract class EntityCreature(type: EntityType) : EntityLiving(type), InventoryHolder {

    override val inventory by lazy { CreatureInventory(this) }
}
