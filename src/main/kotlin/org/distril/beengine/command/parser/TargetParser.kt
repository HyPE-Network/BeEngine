package org.distril.beengine.command.parser

import org.distril.beengine.command.CommandSender

object TargetParser : Parser() {

    override fun parse(sender: CommandSender, input: String) =
        if (input == "@s" && !sender.isConsole) sender.name else input
}
