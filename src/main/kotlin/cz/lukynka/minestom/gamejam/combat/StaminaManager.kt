package cz.lukynka.minestom.gamejam.combat

import cz.lukynka.minestom.gamejam.minimessage.miniMessage
import cz.lukynka.minestom.gamejam.utils.schedule
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.timer.TaskSchedule

object StaminaManager {

    private val stamina: MutableMap<Player, Int> = mutableMapOf()
    private val maxStamina: MutableMap<Player, Int> = mutableMapOf()

    fun init() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent::class.java) { event ->
            val player = event.player
            removePlayer(player)
        }

        schedule(TaskSchedule.immediate(), TaskSchedule.tick(10)) {
            stamina.keys.forEach { player ->
                if (getStamina(player) < getMaxStamina(player)) {
                    stamina[player] = (getStamina(player) + 1).coerceIn(0, getMaxStamina(player))
                    player.isAllowFlying = true
                }
            }
        }
    }

    fun getStamina(player: Player): Int {
        if (stamina[player] == null) {
            resetStamina(player)
        }
        return stamina[player]!!
    }

    fun getMaxStamina(player: Player): Int {
        if (maxStamina[player] == null) {
            resetStamina(player)
        }
        return maxStamina[player]!!
    }

    fun removeStamina(player: Player, amount: Int = 1) {
        stamina[player] = stamina[player]!! - amount
        if (stamina[player]!! == 0) {
            player.isAllowFlying = false
        }
    }

    fun resetStamina(player: Player) {
        stamina[player] = 3
        maxStamina[player] = 3
    }

    fun addMaxStamina(player: Player, amount: Int = 1) {
        maxStamina[player] = maxStamina[player]!! + amount
    }

    fun removePlayer(player: Player) {
        maxStamina.remove(player)
        stamina.remove(player)
    }

    fun sendActionBar(player: Player) {
        val bar = buildString {
            append("Stamina: <gray>[")
            val empty = player.maxStamina - player.stamina
            val full = player.stamina

            for (i in 0 until full) {
                append("<aqua>█")
            }
            for (i in 0 until empty) {
                append("<dark_gray>█")
            }

            append("<gray>]")
        }
        player.sendActionBar(bar.miniMessage)
    }
}

val Player.stamina: Int get() = StaminaManager.getStamina(this)
val Player.maxStamina: Int get() = StaminaManager.getMaxStamina(this)
