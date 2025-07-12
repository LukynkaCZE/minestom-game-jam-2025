package cz.lukynka.minestom.gamejam.combat.enemy

import cz.lukynka.minestom.gamejam.extensions.ticks
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.ai.goal.MeleeAttackGoal
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget
import net.minestom.server.entity.attribute.Attribute
import kotlin.time.toJavaDuration


abstract class AbstractEnemy(
    entityType: EntityType
) : EntityCreature(entityType) {
    init {
        addAIGroup(
            listOf(
                MeleeAttackGoal(this, 1.6, 20.ticks.toJavaDuration()),
                RandomStrollGoal(this, 5),
                RandomLookAroundGoal(this, 20)
            ),
            listOf(
                LastEntityDamagerTarget(this, 32f),
                ClosestEntityTarget(this, 32.0) { it is Player && !it.isInvulnerable }
            )
        )

        this.getAttribute(Attribute.MOVEMENT_SPEED)
            .baseValue = 0.1
    }
}