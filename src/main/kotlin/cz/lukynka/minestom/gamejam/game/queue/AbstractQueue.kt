package cz.lukynka.minestom.gamejam.game.queue

import cz.lukynka.minestom.gamejam.apis.Bossbar
import cz.lukynka.minestom.gamejam.game.GameInstanceImpl
import cz.lukynka.minestom.gamejam.truncate
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.entity.Player
import java.util.Collections

abstract class AbstractQueue : Queue {
    protected val _players = ObjectArrayList<Player>()
    override val players: List<Player> = Collections.unmodifiableList(_players)

    protected val bar = Bossbar(bossBarTitle(), 0f, BossBar.Color.PURPLE, BossBar.Overlay.NOTCHED_20)

    override fun enqueue(player: Player) {
        if (players.contains(player)) return

        _players.addLast(player)
        bar.addViewer(player)
        bar.title.value = bossBarTitle()
    }

    override fun dequeue(player: Player) {
        _players.remove(player)
        bar.removeViewer(player)
    }

    override fun makeTeam(): Result<List<Player>> {
        return forceMakeTeam()
    }

    protected abstract fun bossBarTitle(): String
}