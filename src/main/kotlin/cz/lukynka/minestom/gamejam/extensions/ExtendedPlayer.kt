package cz.lukynka.minestom.gamejam.extensions


import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.minimessage.miniMessage
import cz.lukynka.minestom.gamejam.types.Location
import net.kyori.adventure.sound.Sound
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.SendablePacket
import net.minestom.server.network.packet.server.play.*
import net.minestom.server.sound.SoundEvent
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


fun Player.playSound(sound: AbstractEnemy.SoundRange, location: Pos, volume: Float = 0.8f) {
    this.playSound(sound.sound, location, sound.pitchRange.random(), volume)
}

fun Player.playSound(sound: SoundEvent, location: Pos, volume: Float = 1f, pitch: Float = 1f) {
    this.sendPacket(SoundEffectPacket(sound, Sound.Source.MASTER, location, volume, pitch, 0))
}
