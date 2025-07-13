package cz.lukynka.minestom.gamejam.combat

import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.extensions.location
import cz.lukynka.minestom.gamejam.extensions.playSound
import cz.lukynka.minestom.gamejam.extensions.spawnParticle
import cz.lukynka.minestom.gamejam.vectors.Vector3d
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.EquipmentSlot
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.entity.EntityDeathEvent
import net.minestom.server.item.Material
import net.minestom.server.particle.Particle
import net.minestom.server.sound.SoundEvent

object CombatManager {

    private val GLOBAL_EVENT_HANDLER = MinecraftServer.getGlobalEventHandler()
    private val SWEEP_SOUND = AbstractEnemy.SoundRange(SoundEvent.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f..1.3f)

    fun init() {
        GLOBAL_EVENT_HANDLER.addListener(EntityDamageEvent::class.java) { event ->
            val entity = event.entity
            event.sound = null
            if (entity is AbstractEnemy) {
                entity.playSound(entity.sounds.damageSound)
            }
        }

        GLOBAL_EVENT_HANDLER.addListener(EntityDeathEvent::class.java) { event ->
            val entity = event.entity
            if (entity is AbstractEnemy) {
                entity.playSound(entity.sounds.deathSound)
            }
        }

        GLOBAL_EVENT_HANDLER.addListener(EntityAttackEvent::class.java) { event ->
            val victim = event.target
            val attacker = event.entity

            var damage = 2f
            if (attacker is AbstractEnemy) {
                damage = attacker.damage
            }

            if (attacker is Player) {
                if (attacker.inventory.getEquipment(EquipmentSlot.MAIN_HAND, attacker.heldSlot).material() != Material.IRON_SWORD) return@addListener
                val direction = attacker.location.getDirection() * Vector3d(1.8)
                val particleLocation = attacker.location.add(0.0, attacker.entityType.height() - 0.2, 0.0).add(direction)

                event.instance.playSound(SWEEP_SOUND, event.target.position)
                event.instance.spawnParticle(Particle.SWEEP_ATTACK, particleLocation.toMinestom())
            }

            if (victim is LivingEntity) {
                victim.damage(DamageType.MOB_ATTACK, damage)
            }
        }
    }

}