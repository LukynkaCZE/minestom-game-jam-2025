package cz.lukynka.minestom.gamejam.combat

import cz.lukynka.minestom.gamejam.extensions.location
import cz.lukynka.minestom.gamejam.extensions.playSound
import cz.lukynka.minestom.gamejam.vectors.Vector3d
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerStartFlyingEvent
import net.minestom.server.sound.SoundEvent

object DoubleJump {

    private val GLOBAL_EVENT_HANDLER = MinecraftServer.getGlobalEventHandler()
    private val NEW_VELOCITY_BASE = Vector3d(0.0, 1.0, 0.0)
    private val UP = Vector3d(0.0, 2.0, 0.0)
    private val VELOCITY_MULT = 12.0

    fun init() {
        GLOBAL_EVENT_HANDLER.addListener(PlayerStartFlyingEvent::class.java) { event ->
            val player = event.player
            player.isFlying = false
            val direction = player.location.getDirection(true)
            val cross = UP.cross(direction)
            val rightVector = if (cross.length() > 0.0) cross.normalized() else Vector3d(1.0, 0.0, 0.0)

            var newVelocity = NEW_VELOCITY_BASE

            if (player.inputs().forward()) {
                newVelocity += direction * VELOCITY_MULT
            }

            if (player.inputs().backward()) {
                newVelocity -= direction * VELOCITY_MULT
            }

            if (player.inputs().left()) {
                newVelocity += rightVector * VELOCITY_MULT
            }


            if (player.inputs().right()) {
                newVelocity -= rightVector * VELOCITY_MULT
            }

            player.velocity = newVelocity.toMinestomVec()
            player.playSound(SoundEvent.ENTITY_ENDER_DRAGON_FLAP, player.position, 1f, 1f)
        }
    }
}