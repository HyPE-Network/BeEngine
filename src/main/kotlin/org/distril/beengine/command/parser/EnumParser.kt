package org.distril.beengine.command.parser

import com.nukkitx.protocol.bedrock.data.command.CommandEnumData
import org.distril.beengine.command.CommandSender

class EnumParser : Parser() {

    private val values = mutableSetOf<String>()

    fun addValues(enumData: CommandEnumData) {
        this.values.addAll(listOf(*enumData.values))
    }

    override fun parse(sender: CommandSender, input: String) = if (this.values.contains(input)) input else null
}
