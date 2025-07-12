package cz.lukynka.minestom.gamejam.extensions


import cz.lukynka.minestom.gamejam.minimessage.miniMessage
import cz.lukynka.minestom.gamejam.types.Location
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.SendablePacket
import net.minestom.server.network.packet.server.play.SetTitleSubTitlePacket
import net.minestom.server.network.packet.server.play.SetTitleTextPacket
import net.minestom.server.network.packet.server.play.SetTitleTimePacket
import net.minestom.server.network.packet.server.play.SystemChatPacket
import kotlin.time.Duration

fun Player.sendTitle(title: String, subtitle: String, fadeIn: Duration, stay: Duration, fadeOut: Duration) {
    val titlePacket = SetTitleTextPacket(title.miniMessage)
    val subtitlePacket = SetTitleSubTitlePacket(subtitle.miniMessage)
    val timesPacket = SetTitleTimePacket(fadeIn.inWholeMinecraftTicks, stay.inWholeMinecraftTicks, fadeIn.inWholeMinecraftTicks)
    this.sendPackets(titlePacket, subtitlePacket, timesPacket)
}

fun Player.send(message: String) {
    this.sendPacket(SystemChatPacket(message.miniMessage, false))
}

fun Player.sendActionBar(text: String) {
    this.sendPacket(SystemChatPacket(text.miniMessage, true))
}

val Player.location: Location get() = this.position.toLocation(this.instance)

// lists

fun Collection<Player>.sendActionBar(text: String) {
    this.forEach { player ->
        player.sendActionBar(text)
    }
}

fun Collection<Player>.sendTitle(title: String, subtitle: String, fadeIn: Duration, stay: Duration, fadeOut: Duration) {
    this.forEach { player ->
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut)
    }
}

fun Collection<Player>.sendPacket(packet: SendablePacket) {
    this.forEach { player -> player.sendPacket(packet) }
}