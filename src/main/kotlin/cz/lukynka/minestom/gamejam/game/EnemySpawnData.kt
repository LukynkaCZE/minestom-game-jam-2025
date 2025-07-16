package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.combat.ElementType
import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.entity.Runner
import cz.lukynka.minestom.gamejam.entity.Zombie

data class EnemySpawnData(val weight: Int, val supplier: () -> AbstractEnemy)

object EnemySpawns {

    val difficultyToAmount: Map<IntRange, IntRange> = mapOf(
        (1..1) to 3..3,
        (2..2) to 5..5,
        (3..8) to 6..9,
        (9..9) to 3..3,
        (10..Int.MAX_VALUE) to 5..9,
    )

    val difficultyToEnemies: Map<IntRange, List<EnemySpawnData>> = mapOf(
        (1..3) to listOf(
            EnemySpawnData(100) { Zombie(null) }
        ),
        (3..4) to listOf(
            EnemySpawnData(50) { Zombie(ElementType.ELECTRICAL) },
            EnemySpawnData(50) { Zombie(ElementType.FIRE) }
        ),
        (5..8) to listOf(
            EnemySpawnData(100) { Zombie(ElementType.entries.random()) },
        ),

        (9..9) to listOf(
            EnemySpawnData(100) { Runner(ElementType.entries.random()) },
        ),

        (10..12) to listOf(
            EnemySpawnData(80) { Zombie(ElementType.entries.random()) },
            EnemySpawnData(20) { Runner(ElementType.FIRE) }
        ),

        (12..Int.MAX_VALUE) to listOf(
            EnemySpawnData(60) { Zombie(ElementType.entries.random()) },
            EnemySpawnData(40) { Runner(ElementType.ICE) }
        ),

        )
}