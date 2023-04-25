package org.distril.beengine.command

interface CommandSender {

	val name: String

	val isConsole: Boolean
		get() = false

	fun sendMessage(message: String)

	fun hasPermission(permission: String): Boolean
}
