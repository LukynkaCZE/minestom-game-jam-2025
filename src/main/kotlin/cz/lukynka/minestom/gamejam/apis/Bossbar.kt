package cz.lukynka.minestom.gamejam.apis

import cz.lukynka.bindables.BindablePool
import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.DockyardViewable
import cz.lukynka.minestom.gamejam.event.EventPool
import cz.lukynka.minestom.gamejam.extensions.sendPacket
import cz.lukynka.minestom.gamejam.minimessage.miniMessage
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.network.packet.server.play.BossBarPacket
import java.util.*

class Bossbar(title: String = "", progress: Float = 1f, color: BossBar.Color, notches: BossBar.Overlay) : DockyardViewable(), Disposable {

    val uuid: UUID = UUID.randomUUID()

    private val eventPool = EventPool()
    private val bindablePool = BindablePool()

    val title = bindablePool.provideBindable(title)
    val progress = bindablePool.provideBindable(progress)
    val color = bindablePool.provideBindable(color)
    val notches = bindablePool.provideBindable(notches)

    init {

        this.title.valueChanged { event ->
            val action = BossBarPacket.UpdateTitleAction(event.newValue.miniMessage)
            viewers.sendPacket(BossBarPacket(uuid, action))
        }

        this.progress.valueChanged { event ->
            val action = BossBarPacket.UpdateHealthAction(event.newValue.coerceIn(0f, 1f))
            viewers.sendPacket(BossBarPacket(uuid, action))
        }

        this.color.valueChanged { _ ->
            val action = BossBarPacket.UpdateStyleAction(this.color.value, this.notches.value)
            viewers.sendPacket(BossBarPacket(uuid, action))
        }

        this.notches.valueChanged { _ ->
            val action = BossBarPacket.UpdateStyleAction(this.color.value, this.notches.value)
            viewers.sendPacket(BossBarPacket(uuid, action))
        }

        eventPool.on<PlayerDisconnectEvent> { event ->
            removeViewer(event.player)
        }
    }

    override fun addViewer(player: Player): Boolean {
        if (!super.addViewer(player)) return false

        val action = BossBarPacket.AddAction(
            title.value.miniMessage,
            progress.value,
            color.value,
            notches.value,
            0
        )

        player.sendPacket(BossBarPacket(uuid, action))
        return true
    }

    override fun removeViewer(player: Player) {
        player.sendPacket(BossBarPacket(uuid, BossBarPacket.RemoveAction()))
    }

    override fun dispose() {
        bindablePool.dispose()
        eventPool.dispose()
        this.clearViewers()
    }
}