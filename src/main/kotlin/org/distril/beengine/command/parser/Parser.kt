package org.distril.beengine.command.parser

import org.distril.beengine.command.CommandSender

abstract class Parser {

    var optional = false

    abstract fun parse(sender: CommandSender, input: String): String?
}
