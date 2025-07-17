package cz.lukynka.minestom.gamejam.extensions

import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.utils.WorldAudience
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.minestom.server.network.packet.server.play.SoundEffectPacket
import net.minestom.server.sound.SoundEvent

fun Audience.sendMessage(msg: String) = sendMessage(Component.text(msg))

fun Audience.playSound(sound: AbstractEnemy.SoundRange, volume: Float = 0.8f) {
    this.playSound(sound.sound, sound.pitchRange.random(), volume)
}

fun Audience.playSound(sound: SoundEvent, volume: Float = 1f, pitch: Float = 1f) {
    this.playSound(Sound.sound(sound, Sound.Source.MASTER, volume, pitch))
}