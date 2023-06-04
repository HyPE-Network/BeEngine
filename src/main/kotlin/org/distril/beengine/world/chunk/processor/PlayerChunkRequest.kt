package org.distril.beengine.world.chunk.processor

import org.distril.beengine.player.Player
import org.distril.beengine.world.chunk.Chunk

abstract class PlayerChunkRequest(val player: Player, val chunk: Chunk)

class SendChunk(player: Player, chunk: Chunk) : PlayerChunkRequest(player, chunk)
