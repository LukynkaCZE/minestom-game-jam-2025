package cz.lukynka.minestom.gamejam.game.queue

import cz.lukynka.minestom.gamejam.game.GameInstanceImpl
import cz.lukynka.minestom.gamejam.utils.truncate
import net.minestom.server.entity.Player

class PublicQueue : AbstractQueue() {
    companion object {
        const val PLAYERS_FOR_SINGLE_GAME = 4 // did you say 4? I don't remember
        const val LOBBY_WAIT_TIME_TICKS = 10 * 20
    }

    private var timer = LOBBY_WAIT_TIME_TICKS

    fun tick() {
        if (_players.isEmpty) {
            timer = LOBBY_WAIT_TIME_TICKS
            return
        }

        if (--timer <= 0) {
            // timer is over, force the game
            forceMakeTeam()
                .onSuccess { players ->
                    val instance = GameInstanceImpl(players)
                    // TODO
                }
            timer = LOBBY_WAIT_TIME_TICKS
            bar.progress.value = 0f
            bar.title.value = bossBarTitle()
        } else {
            // try and make a full sized team
            makeTeam()
                .onSuccess { players ->
                    val instance = GameInstanceImpl(players)
                    // TODO
                    timer = LOBBY_WAIT_TIME_TICKS
                }
            bar.progress.value = 1 - (timer.toFloat() / LOBBY_WAIT_TIME_TICKS)
            bar.title.value = bossBarTitle()
        }
    }

    override fun makeTeam(): Result<List<Player>> {
        if (_players.size >= PLAYERS_FOR_SINGLE_GAME) {
            val result = _players.subList(0, PLAYERS_FOR_SINGLE_GAME)
            result.forEach(::dequeue)
            return Result.success(result)
        }
        return Result.failure(NoSuchElementException("not enough players for a game. ${_players.size} < $PLAYERS_FOR_SINGLE_GAME"))
    }

    override fun forceMakeTeam(): Result<List<Player>> {
        if (_players.isEmpty) return Result.failure(NoSuchElementException("`players` is empty"))

        val result = _players.take(PLAYERS_FOR_SINGLE_GAME)
        result.forEach(::dequeue)
        return Result.success(result)
    }

    override fun bossBarTitle(): String {
        return "Queue: ${_players.size}/$PLAYERS_FOR_SINGLE_GAME [${(timer.toFloat() / 20).truncate(1)}s]"
    }
}