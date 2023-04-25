package org.distril.beengine.material.block.state

class StringState(property: String, defaultValue: String) : State<String>(property, defaultValue) {

	constructor(property: String, defaultValue: Enum<*>) : this(property, defaultValue.name)
}
