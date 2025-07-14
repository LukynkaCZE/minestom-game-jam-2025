package cz.lukynka.minestom.gamejam.game.queue

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minestom.server.entity.Player
import java.util.Collections

class PrivateQueueImpl(override val owner: Player) : AbstractQueue(), PrivateQueue {
    private val _invitedPlayers = ObjectArrayList<Player>()
    override val invitedPlayers: List<Player> = Collections.unmodifiableList(_invitedPlayers)

    init {
        enqueue(owner)
    }

    override fun invite(player: Player) {
        _invitedPlayers.add(player)
    }

    override fun enqueue(player: Player) {
        super.enqueue(player)
        _invitedPlayers.remove(player)
        bar.progress.value = (_players.size / 4f).coerceAtMost(1f)
    }

    override fun dequeue(player: Player) {
        if (player == owner) {
            dispose()
        } else {
            super.dequeue(player)
        }
    }

    override fun forceMakeTeam(): Result<List<Player>> {
        if (_players.isEmpty) {
            return Result.failure(NoSuchElementException("`players` is empty"))
        }

        val team = _players.toList()
        team.forEach(::dequeue)

        return Result.success(team)
    }

    override fun bossBarTitle(): String {
        return "Private Lobby: ${_players.size}"
    }
}