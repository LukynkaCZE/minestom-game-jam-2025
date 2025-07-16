package cz.lukynka.minestom.gamejam.entity

import cz.lukynka.minestom.gamejam.combat.ElementType
import cz.lukynka.minestom.gamejam.extensions.ticks
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.ai.goal.MeleeAttackGoal
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.sound.SoundEvent
import kotlin.time.toJavaDuration

class Runner(type: ElementType?) : AbstractEnemy(EntityType.PIGLIN, type, "Runner") {

    override val sounds: Sounds = Sounds(
        damageSound = SoundRange(SoundEvent.ENTITY_ZOMBIE_HURT, 0.8f..1.2f),
        deathSound = SoundRange(SoundEvent.ENTITY_ZOMBIE_DEATH),
        ambientSound = SoundRange(SoundEvent.ENTITY_ZOMBIE_AMBIENT, 0.8f..1.2f)
    )

    override val damage: Float = 1f
    override val enemyHealth: Float = 20f

    init {
        this.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.40

        addAIGroup(
            listOf(
                MeleeAttackGoal(this, 1.2, 20.ticks.toJavaDuration()),
                RandomStrollGoal(this, 5),
                RandomLookAroundGoal(this, 20)
            ),
            listOf(
                LastEntityDamagerTarget(this, 32f),
                ClosestEntityTarget(this, 32.0) { it is Player && !it.isInvulnerable }
            )
        )
        this.health = enemyHealth
    }
}