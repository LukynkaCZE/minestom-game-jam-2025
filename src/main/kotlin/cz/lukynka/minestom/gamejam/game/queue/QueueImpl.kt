package cz.lukynka.minestom.gamejam.game.queue

import cz.lukynka.minestom.gamejam.apis.Bossbar
import cz.lukynka.minestom.gamejam.game.GameInstanceImpl
import cz.lukynka.minestom.gamejam.truncate
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.entity.Player
import java.util.Collections

open class QueueImpl : Queue {
    companion object Instance : QueueImpl() {
        const val PLAYERS_FOR_SINGLE_GAME = 4 // did you say 4? I don't remember
        const val LOBBY_WAIT_TIME_TICKS = 10 * 20
    }

    private val _players = ObjectArrayList<Player>()
    override val players: List<Player> = Collections.unmodifiableList(_players)
    private var timer = LOBBY_WAIT_TIME_TICKS

    private val bar = Bossbar("", 0f, BossBar.Color.PURPLE, BossBar.Overlay.NOTCHED_20)

    override fun tick() {
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

    override fun enqueue(player: Player) {
        if (players.contains(player)) return

        _players.addLast(player)
        bar.addViewer(player)
    }

    override fun dequeue(player: Player) {
        _players.remove(player)
        bar.removeViewer(player)
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

    private fun bossBarTitle(): String {
        return "Queue: ${_players.size}/$PLAYERS_FOR_SINGLE_GAME [${(timer.toFloat() / 20).truncate(1)}s]"
    }
}