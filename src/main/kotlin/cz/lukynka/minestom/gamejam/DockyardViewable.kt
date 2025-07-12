package cz.lukynka.minestom.gamejam

import cz.lukynka.minestom.gamejam.extensions.sendPacket
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.SendablePacket

abstract class DockyardViewable {

    private val innerViewers: ObjectOpenHashSet<Player> = ObjectOpenHashSet()
    val viewers: List<Player> get() = innerViewers.toList()

    private val innerRules: MutableMap<String, Rule> = mutableMapOf()
    val rules get() = innerRules.toMap()

    fun addViewRule(identifier: String, filter: (Player) -> Boolean) {
        if (innerRules.containsKey(identifier)) throw IllegalArgumentException("View Rule with identifier `$identifier` already exists on this viewable")
        innerRules[identifier] = Rule(filter)
    }

    fun passesViewRules(player: Player): Boolean {
        return rules.all { (_, rule) -> rule.passes(player) }
    }

    open fun addViewer(player: Player): Boolean {
        if (rules.all { (_, rule) -> rule.passes(player) && !viewers.contains(player) }) {
            synchronized(innerViewers) {
                innerViewers.add(player)
            }
            return true
        }
        return false
    }

    open fun removeViewer(player: Player) {
        synchronized(innerViewers) {
            innerViewers.remove(player)
        }
    }

    fun isViewer(player: Player): Boolean {
        return viewers.contains(player)
    }

    fun sendPacketToViewers(packet: SendablePacket) {
        viewers.sendPacket(packet)
    }

    fun clearViewers() {
        viewers.toList().forEach { viewer ->
            removeViewer(viewer)
        }
    }

    data class Rule(private val filter: (Player) -> Boolean) {

        fun passes(player: Player): Boolean {
            return filter.invoke(player)
        }

    }
}