package cz.lukynka.minestom.gamejam.game.queue

import net.minestom.server.entity.Player

/**
 * Queue for when players want to play in private groups (invite-only)
 */
interface PrivateQueue : Queue {
    /**
     * Owner of the group
     */
    val owner: Player

    val invitedPlayers: List<Player>

    fun invite(player: Player)
}