package org.distril.beengine.player.data

import com.nukkitx.math.vector.Vector3f
import org.distril.beengine.server.Server
import org.distril.beengine.world.util.Location

class PlayerData {

    var pitch = 0f
    var yaw = 0f
    var headYaw = 0f
    var location = Location(
        Server.worldRegistry.defaultWorld,
        Vector3f.from(0f, 60f, 0f)
    )
    var gameMode = GameMode.CREATIVE
}
