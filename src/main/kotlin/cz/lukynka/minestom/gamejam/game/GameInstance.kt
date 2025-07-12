@file:Suppress("OVERRIDE_DEPRECATION", "UnstableApiUsage", "DEPRECATION") // don't we all love adventure?

package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.utils.PlayerListAudience
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance

interface GameInstance : PlayerListAudience {
    override val players: List<Player>
    val world: Instance
}