package cz.lukynka.minestom.gamejam.extensions

import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.types.Location
import cz.lukynka.minestom.gamejam.vectors.Vector3d
import net.kyori.adventure.sound.Sound
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.network.packet.server.play.SoundEffectPacket
import net.minestom.server.particle.Particle
import net.minestom.server.sound.SoundEvent

fun Instance.locationAt(x: Int, y: Int, z: Int): Location {
    return Location(x, y, z, this)
}

fun Instance.getBlock(location: Location): Block {
    return this.getBlock(location.toMinestom())
}

fun Instance.playSound(sound: AbstractEnemy.SoundRange, location: Pos, volume: Float = 0.8f) {
    this.playSound(sound.sound, location, sound.pitchRange.random(), volume)
}

fun Instance.playSound(sound: SoundEvent, location: Pos, volume: Float = 1f, pitch: Float = 1f) {
    this.players.sendPacket(SoundEffectPacket(sound, Sound.Source.MASTER, location, volume, pitch, 0))
}

fun Instance.spawnParticle(particle: Particle, location: Pos, offset: Vector3d = Vector3d(0.0), speed: Float = 0.0f, amount: Int = 1, alwaysShow: Boolean = false) {
    this.players.sendPacket(ParticlePacket(particle, false, false, location, Vec(offset.x, offset.y, offset.z), speed, amount))
}