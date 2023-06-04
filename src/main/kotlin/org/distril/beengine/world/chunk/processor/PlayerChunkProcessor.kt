package org.distril.beengine.world.chunk.processor

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.distril.beengine.util.ModuleScope
import org.distril.beengine.util.Utils.getLogger

class PlayerChunkProcessor(worldName: String) {

    private val scope = ModuleScope("PlayerChunkProcessor for $worldName world")

    private val requests = Channel<PlayerChunkRequest>(Channel.UNLIMITED)

    init {
        this.scope.launch {
            while (true) {
                val request = requests.receive()
                if (request is SendChunk) {
                    val player = request.player

                    val chunk = request.chunk
                    player.sendPacket(chunk.createPacket())

                    val isNotLoaded = player.world.chunkManager.getLoadedChunk(chunk.hash()) == null
                    if (isNotLoaded) {
                        log.warn(
                            "Attempted to send unloaded chunk (${chunk.x}:${chunk.z}) to " + player.name
                        )

                        continue
                    }

                    player.chunkManager.succesfull(chunk.hash())

                    chunk.entities.forEach { if (it != player && !it.isSpawned) it.spawnFor(player) }
                }
            }
        }
    }

    fun addRequest(request: PlayerChunkRequest) {
        this.scope.launch { requests.send(request) }
    }

    companion object {

        val log = PlayerChunkProcessor.getLogger()
    }
}
