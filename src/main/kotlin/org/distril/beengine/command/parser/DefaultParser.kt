package org.distril.beengine.command.parser

import org.distril.beengine.command.CommandSender

object DefaultParser : Parser() {

    override fun parse(sender: CommandSender, input: String) = input
}
