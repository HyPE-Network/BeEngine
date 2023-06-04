package org.distril.beengine.command.data

import org.distril.beengine.player.Player
import org.distril.beengine.player.data.GameMode
import org.distril.beengine.server.Server
import java.util.*

class Args(private val args: Map<String, String>) {

    fun getInteger(key: String): Int? = try {
        this.args[key]?.toInt()
    } catch (_: NumberFormatException) {
        null
    }

    fun getFloat(key: String): Float? = try {
        this.args[key]?.toFloat()
    } catch (_: NumberFormatException) {
        null
    }

    fun getTarget(key: String): Player? {
        val username = this.getString(key)

        return username?.let {
            if (it == "@r") return Server.players.toList()[Random().nextInt(Server.players.size)]

            return Server.getPlayer(it)
        }
    }

    fun getString(key: String) = this.args[key]

    fun getGameMode(key: String): GameMode? {
        val id = this.getInteger(key)
        return if (id != null) GameMode.fromId(id) else GameMode.fromIdentifierOrAlias(this.getString(key)!!)
    }

    fun has(key: String) = this.args.containsKey(key)

    fun isEmpty() = this.args.isEmpty()
}
