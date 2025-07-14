package cz.lukynka.minestom.gamejam.combat

import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.extensions.*
import cz.lukynka.minestom.gamejam.types.Location
import cz.lukynka.minestom.gamejam.vectors.Vector3d
import net.minestom.server.MinecraftServer
import net.minestom.server.color.Color
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.*
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.entity.metadata.projectile.FireworkRocketMeta
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.entity.EntityDeathEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.FireworkExplosion
import net.minestom.server.particle.Particle
import net.minestom.server.sound.SoundEvent

object CombatManager {

    private val GLOBAL_EVENT_HANDLER = MinecraftServer.getGlobalEventHandler()
    private val SWEEP_SOUND = AbstractEnemy.SoundRange(SoundEvent.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f..1.5f)
    private val EXPLOSION_RADIUS = 2.5f

    fun init() {
        GLOBAL_EVENT_HANDLER.addListener(EntityDamageEvent::class.java) { event ->
            val entity = event.entity
            event.sound = null
            if (entity is AbstractEnemy) {
                entity.playSound(entity.sounds.damageSound)
                entity.instance.spawnBlood(entity.position, Vector3d(0.2, 1.0, 0.2), 100)
            }
        }

        GLOBAL_EVENT_HANDLER.addListener(EntityDeathEvent::class.java) { event ->
            val entity = event.entity
            if (entity is AbstractEnemy) {
                entity.playSound(entity.sounds.deathSound)
                entity.elementType.value?.let { explodeElement(it, entity.position.toLocation(entity.instance), entity) }
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

    fun explodeElement(elementType: ElementType, location: Location, causingEntity: LivingEntity) {
        val radius = EXPLOSION_RADIUS //TODO + buffs bought in the shop
        location.world.spawnParticle(Particle.DUST.withScale(2f).withColor(Color(elementType.glowColor.red(), elementType.glowColor.green(), elementType.glowColor.blue())), location.toMinestom(), Vector3d(1.0), speed = 0.2f, amount = 100)

        location.world.entities.filter { entity -> entity.position.distance(location.toMinestom()) <= radius }.forEach { entity ->
            if (entity == causingEntity) return@forEach

            if (entity !is AbstractEnemy) return@forEach
            if (entity.isDead) return@forEach

            if (entity.elementType.value == null) return@forEach
            val reaction = getElementReaction(elementType, entity.elementType.value!!) ?: return@forEach
            explodeReaction(reaction, location, causingEntity)
        }
    }

    fun explodeReaction(reaction: ElementReaction, location: Location, causingEntity: LivingEntity) {
        val radius = EXPLOSION_RADIUS //TODO + buffs bought in the shop
        location.world.entities.filter { entity -> entity.position.distance(location.toMinestom()) <= radius }.forEach { entity ->

            if (entity == causingEntity) return@forEach
            if (entity !is AbstractEnemy) return@forEach
            if (entity.isDead) return@forEach

            val element = entity.elementType.value ?: return@forEach
            entity.damage(DamageType.EXPLOSION, 6f)
            entity.instance.spawnParticle(Particle.DUST.withScale(1f).withColor(Color(element.glowColor.red(), element.glowColor.green(), element.glowColor.blue())), location.toMinestom(), Vector3d(0.5), speed = 0.2f, amount = 100)


            val firework = Entity(EntityType.FIREWORK_ROCKET)
            val meta = firework.entityMeta as FireworkRocketMeta
            meta.fireworkInfo = ItemStack.builder(Material.FIREWORK_ROCKET).set(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, listOf(), listOf(), false, false)).build()

            // chain reactions!!! thats the theme!!
            if (reaction.first == element || reaction.second == element) {
                explodeReaction(reaction, entity.position.toLocation(entity.instance), entity)
            }
        }
    }
}