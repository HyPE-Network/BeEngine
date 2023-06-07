package org.distril.beengine.world.chunk.processor

import org.distril.beengine.player.Player
import org.distril.beengine.world.chunk.Chunk
import java.util.*

abstract class PlayerChunkRequest(val player: Player, val chunk: Chunk) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is PlayerChunkRequest) return false

		if (this.player != other.player) return false
		return this.chunk == other.chunk
	}

	override fun hashCode() = Objects.hash(this.player, this.chunk)
}

class SendChunk(player: Player, chunk: Chunk) : PlayerChunkRequest(player, chunk)
