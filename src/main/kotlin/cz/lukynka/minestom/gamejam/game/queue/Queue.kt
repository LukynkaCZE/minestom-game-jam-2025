package cz.lukynka.minestom.gamejam.game.queue

import cz.lukynka.minestom.gamejam.utils.PlayerListAudience
import net.minestom.server.entity.Player

interface Queue : PlayerListAudience {
    override val players: List<Player>

    /**
     * Put player in queue
     */
    fun enqueue(player: Player)

    /**
     * Remove player from the queue
     */
    fun dequeue(player: Player)

    /**
     * Returns a list of players for a single game if theres enough players
     */
    fun makeTeam(): Result<List<Player>>

    /**
     * Returns a list of players for a single game, if there's at least one player in queue
     */
    fun forceMakeTeam(): Result<List<Player>>
}