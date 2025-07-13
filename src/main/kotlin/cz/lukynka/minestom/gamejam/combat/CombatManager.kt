package cz.lukynka.minestom.gamejam.combat

import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.extensions.random
import net.kyori.adventure.sound.Sound
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.entity.EntityDeathEvent
import net.minestom.server.network.packet.server.play.SoundEffectPacket

object CombatManager {

    private val GLOBAL_EVENT_HANDLER = MinecraftServer.getGlobalEventHandler()

    fun init() {
        GLOBAL_EVENT_HANDLER.addListener(EntityDamageEvent::class.java) { event ->
            val entity = event.entity
            event.sound = null
            if (entity is AbstractEnemy) {
                val soundRange = entity.sounds.damageSound
                entity.sendPacketToViewersAndSelf(SoundEffectPacket(soundRange.sound, Sound.Source.HOSTILE, entity.getPosition(), 0.8f, soundRange.pitchRange.random(), 0))
            }
        }

        GLOBAL_EVENT_HANDLER.addListener(EntityDeathEvent::class.java) { event ->
            val entity = event.entity
            if (entity is AbstractEnemy) {
                val soundRange = entity.sounds.deathSound
                entity.sendPacketToViewersAndSelf(SoundEffectPacket(soundRange.sound, Sound.Source.HOSTILE, entity.getPosition(), 0.8f, soundRange.pitchRange.random(), 0))
            }
        }

        GLOBAL_EVENT_HANDLER.addListener(EntityAttackEvent::class.java) { event ->
            val victim = event.target
            val attacker = event.entity

            var damage: Float = 0f
            if (attacker is AbstractEnemy) {
                damage = attacker.damage
            }

            if (victim is LivingEntity) {
                victim.damage(DamageType.MOB_ATTACK, damage)
            }
        }
    }

}