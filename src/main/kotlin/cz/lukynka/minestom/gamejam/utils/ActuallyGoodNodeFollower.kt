package cz.lukynka.minestom.gamejam.utils

import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import net.minestom.server.collision.CollisionUtils
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.pathfinding.followers.NodeFollower
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Suppress("UnstableApiUsage")
class ActuallyGoodNodeFollower(val entity: AbstractEnemy) : NodeFollower {

    override fun moveTowards(direction: Point, speed: Double, lookAt: Point) {
        if (!entity.canPathfind) return

        val position: Pos = entity.position

        val dx: Double = direction.x() - position.x()
        val dz: Double = direction.z() - position.z()

        val radians = atan2(dz, dx)
        val speedX = cos(radians) * speed
        val speedZ = sin(radians) * speed

        val physicsResult = CollisionUtils.handlePhysics(entity, Vec(speedX, 0.0, speedZ))

        val entityCollisionResults = CollisionUtils.checkEntityCollisions(entity, physicsResult.newVelocity(), 6.0, { entity1: Entity -> entity1 !== entity }, physicsResult)

        var newPos = Pos.fromPoint(physicsResult.newPosition())

        for (entityCollisionResult in entityCollisionResults) {
            newPos = Pos.fromPoint(entityCollisionResult.collisionPoint())
            if (entityCollisionResult.percentage() == 0.0) {
                val mulval = if (entity.isOnGround()) -0.25 else -0.05
                var newVelocity = entityCollisionResult.direction().mul(mulval, mulval / 2, mulval)
                if (entityCollisionResult.direction().isZero) {
                    newVelocity = Vec(ThreadLocalRandom.current().nextDouble(-1.0, 1.0), 0.0, ThreadLocalRandom.current().nextDouble(-1.0, 1.0))
                }
                this.entity.setVelocity(this.entity.getVelocity().add(newVelocity))
            }
        }

        this.entity.refreshPosition(Pos.fromPoint(newPos).withLookAt(lookAt.withY(position.y())))
    }

    override fun jump(point: Point?, target: Point?) {
        if (entity.isOnGround) {
            jump(4f)
        }
    }

    fun jump(height: Float) {
        entity.velocity = Vec(0.0, (height * 2.5f).toDouble(), 0.0)
    }

    override fun isAtPoint(point: Point): Boolean {
        return entity.position.sameBlock(point)
    }

    override fun movementSpeed(): Double {
        if (entity is LivingEntity) {
            return entity.getAttribute(Attribute.MOVEMENT_SPEED).value
        }

        return 0.1
    }
}