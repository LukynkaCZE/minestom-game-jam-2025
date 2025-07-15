package cz.lukynka.minestom.gamejam.combat

import net.minestom.server.network.packet.server.play.TeamsPacket
import net.minestom.server.scoreboard.Team
import net.minestom.server.scoreboard.TeamBuilder
import net.minestom.server.scoreboard.TeamManager

object TeamManager {

    private val MANAGER = TeamManager()
    val TEAMS: MutableMap<ElementType, Team> = mutableMapOf()

    fun registerTeams() {
        ElementType.entries.forEach { entry ->
            val team = TeamBuilder(entry.displayName, MANAGER)
                .updateCollisionRule(TeamsPacket.CollisionRule.PUSH_OWN_TEAM)
                .updateTeamColor(entry.glowColor)
                .build()
            TEAMS[entry] = team
        }
    }
}