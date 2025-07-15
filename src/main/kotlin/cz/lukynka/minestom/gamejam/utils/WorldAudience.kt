@file:Suppress("OVERRIDE_DEPRECATION", "UnstableApiUsage", "DEPRECATION")

package cz.lukynka.minestom.gamejam.utils

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.minestom.server.instance.Instance

interface WorldAudience : Audience {
    val world: Instance

    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        world.sendMessage(source, message, type)
    }

    override fun showBossBar(bar: BossBar) {
        world.showBossBar(bar)
    }

    override fun hideBossBar(bar: BossBar) {
        world.hideBossBar(bar)
    }
}