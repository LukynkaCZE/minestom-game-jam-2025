package cz.lukynka.minestom.gamejam.combat

import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.extensions.*
import cz.lukynka.minestom.gamejam.minimessage.miniMessage
import cz.lukynka.minestom.gamejam.types.Location
import cz.lukynka.minestom.gamejam.utils.runLater
import cz.lukynka.minestom.gamejam.utils.spawnTextDisplay
import cz.lukynka.minestom.gamejam.utils.strikeLightning
import cz.lukynka.minestom.gamejam.vectors.Vector3d
import net.minestom.server.MinecraftServer
import net.minestom.server.color.Color
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EquipmentSlot
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.entity.EntityDeathEvent
import net.minestom.server.event.player.PlayerSwapItemEvent
import net.minestom.server.event.player.PlayerTickEndEvent
import net.minestom.server.item.Material
import net.minestom.server.particle.Particle
import net.minestom.server.sound.SoundEvent
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

object CombatManager {

    private val GLOBAL_EVENT_HANDLER = MinecraftServer.getGlobalEventHandler()
    private val SWEEP_SOUND = AbstractEnemy.SoundRange(SoundEvent.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f..1.5f)
    private const val EXPLOSION_RADIUS = 3.5f

    val inSlamMode: MutableSet<Player> = mutableSetOf()

    fun init() {
        GLOBAL_EVENT_HANDLER.addListener(PlayerSwapItemEvent::class.java) { event ->
            val player = event.player
            event.isCancelled = true

            if (!inSlamMode.contains(player)) {
                player.velocity = Vec(0.0, 20.0, 0.0)
                runLater(1.ticks) {
                    inSlamMode.add(player)
                }
            } else {
                player.velocity = Vec(0.0, -100.0, 0.0)
            }
        }

        GLOBAL_EVENT_HANDLER.addListener(PlayerTickEndEvent::class.java) { event ->
            val player = event.player
            if (inSlamMode.contains(event.player)) {
                if (!player.isOnGround) return@addListener
                player.instance.entities.filter { entity ->
                    entity != player &&
                            entity is AbstractEnemy &&
                            entity.location.distance(player.location) <= 2
                }.forEach { entity ->
                    (entity as AbstractEnemy).damage(DamageType.MACE_SMASH, 10f)
                }
                inSlamMode.remove(player)

                player.instance.playSound(SoundEvent.ITEM_MACE_SMASH_GROUND_HEAVY, player.position, 0.5f, 1f)
                player.instance.playSound(SoundEvent.ITEM_MACE_SMASH_GROUND_HEAVY, player.position, 0.5f, 0.5f)
                player.instance.playSound(SoundEvent.ITEM_MACE_SMASH_GROUND_HEAVY, player.position, 0.5f, 1.5f)

                val below = player.location.subtract(0, 1, 0)
                player.instance.spawnParticle(Particle.BLOCK.withBlock(below.block), below.add(0.0, 1.5, 0.0).toMinestom(), amount = 50, speed = 0.5f, offset = Vector3d(2.0, 0.2, 2.0))
            }
        }

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
        location.world.spawnParticle(Particle.DUST.withColor(Color(elementType.glowColor.red(), elementType.glowColor.green(), elementType.glowColor.blue())), location.toMinestom(), Vector3d(1.0), speed = 0.2f, amount = 100)
        location.world.spawnParticle(Particle.FLASH, location.toMinestom(), Vector3d(0.0), speed = 0.2f, amount = 1)

        location.world.entities.filter { entity -> entity.position.distance(location.toMinestom()) <= radius }.forEach { entity ->
            if (entity == causingEntity) return@forEach

            if (entity !is AbstractEnemy) return@forEach
            if (entity.isDead) return@forEach

            if (entity.elementType.value == null) return@forEach
            val reaction = getElementReaction(elementType, entity.elementType.value!!) ?: return@forEach

            runLater(10.ticks) {
                explodeReaction(reaction, location, causingEntity)
            }
            return
        }
    }

    fun explodeReaction(reaction: ElementReaction, location: Location, causingEntity: LivingEntity) {
        val radius = EXPLOSION_RADIUS //TODO + buffs bought in the shop
        location.world.entities.filter { entity -> entity.position.distance(location.toMinestom()) <= radius }.forEach { entity ->

            if (entity == causingEntity) return@forEach
            if (entity !is AbstractEnemy) return@forEach
            if (entity.isDead) return@forEach

            val element = entity.elementType.value ?: return@forEach
            entity.damage(DamageType.EXPLOSION, 4f)
            entity.instance.spawnParticle(Particle.DUST.withScale(1f).withColor(Color(element.glowColor.red(), element.glowColor.green(), element.glowColor.blue())), location.toMinestom(), Vector3d(0.5), speed = 0.2f, amount = 100)

            entity.canPathfind = false
            entity.velocity = Vec(Random.nextDouble(5.0, 10.0), 5.0, Random.nextDouble(5.0, 10.0))

            // text
            val text = spawnTextDisplay(location.world, location.toMinestom().add(entity.eyeHeight).add(Random.nextDouble(-1.0, 1.0), Random.nextDouble(-1.0, 1.0), Random.nextDouble(-1.0, 1.0))) {
                text = "${reaction.color}<bold><i>${reaction.displayName}!!".miniMessage
                billboardRenderConstraints = AbstractDisplayMeta.BillboardConstraints.CENTER
            }

            runLater(3.seconds) {
                text.remove()
            }

            // special effects
            when (reaction) {
                ElementReaction.CONDUCTIVE -> {
                    location.world.strikeLightning(location.toMinestom())
                    location.world.spawnParticle(Particle.DRIPPING_WATER, location.toMinestom(), Vector3d(0.5), amount = 30, speed = 0.1f)
                }

                ElementReaction.SUPERCONDUCT -> {
                    location.world.strikeLightning(location.toMinestom())
                    location.world.spawnParticle(Particle.SNOWFLAKE, location.toMinestom(), Vector3d(0.5, 0.8, 0.5), amount = 30, speed = 0.1f)
                }

                ElementReaction.OVERCHARGED -> {
                    location.world.strikeLightning(location.toMinestom())
                    location.world.spawnParticle(Particle.FLAME, location.toMinestom(), Vector3d(0.5, 0.8, 0.5), amount = 30, speed = 0.1f)
                }

                ElementReaction.FROZEN -> {
                    location.world.spawnParticle(Particle.SNOWFLAKE, location.toMinestom(), Vector3d(0.5, 0.8, 0.5), amount = 100, speed = 0.3f)
                }

                ElementReaction.STEAM -> {
                    location.world.spawnParticle(Particle.CLOUD, location.toMinestom(), Vector3d(0.5, 0.8, 0.5), amount = 50, speed = 0.1f)
                    location.world.spawnParticle(Particle.FLAME, location.toMinestom(), Vector3d(0.5, 0.8, 0.5), amount = 50, speed = 0.1f)
                }

                ElementReaction.HYPERMELT -> {
                    location.world.spawnParticle(Particle.DRIPPING_WATER, location.toMinestom(), Vector3d(0.5, 0.8, 0.5), amount = 50, speed = 0.1f)
                    location.world.spawnParticle(Particle.CLOUD, location.toMinestom(), Vector3d(0.5, 0.8, 0.5), amount = 50, speed = 0.1f)
                    location.world.spawnParticle(Particle.FLAME, location.toMinestom(), Vector3d(0.5, 0.8, 0.5), amount = 50, speed = 0.1f)
                }
            }

            runLater(3.ticks) {
                entity.canPathfind = true
            }

            // chain reactions!!! thats the theme!!
            if (reaction.first == element || reaction.second == element) {
                runLater(10.ticks) {
                    explodeReaction(reaction, entity.position.toLocation(entity.instance), entity)
                }
            }
        }
    }
}

fun pushAwayVelocity(initialVelocity: Vector3d, pushAwayFrom: Location, objectLocation: Location): Vector3d {
    val direction = objectLocation.toVector3d() - pushAwayFrom.toVector3d()
    return direction.normalized() * Vector3d(20.0)
}