@file:Suppress("OVERRIDE_DEPRECATION", "UnstableApiUsage", "DEPRECATION") // don't we all love adventure?

package cz.lukynka.minestom.gamejam.utils

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player

interface PlayerListAudience : Audience {
    val players: Collection<Player>

    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        players.forEach {
            it.sendMessage(source, message, type)
        }
    }

    override fun showBossBar(bar: BossBar) {
        players.forEach {
            it.showBossBar(bar)
        }
    }

    override fun hideBossBar(bar: BossBar) {
        players.forEach {
            it.hideBossBar(bar)
        }
    }

    override fun playSound(sound: Sound) {
        players.forEach {
            it.playSound(sound)
        }
    }

    override fun playSound(sound: Sound, emitter: Sound.Emitter) {
        players.forEach {
            it.playSound(sound, emitter)
        }
    }

    override fun playSound(sound: Sound, x: Double, y: Double, z: Double) {
        players.forEach {
            it.playSound(sound, x, y, z)
        }
    }
}