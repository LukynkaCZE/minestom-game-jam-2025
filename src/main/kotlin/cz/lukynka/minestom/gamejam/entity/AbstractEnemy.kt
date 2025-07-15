package cz.lukynka.minestom.gamejam.entity

import cz.lukynka.bindables.Bindable
import cz.lukynka.bindables.BindablePool
import cz.lukynka.minestom.gamejam.combat.ElementType
import cz.lukynka.minestom.gamejam.combat.TeamManager
import cz.lukynka.minestom.gamejam.extensions.random
import cz.lukynka.minestom.gamejam.minimessage.miniMessage
import cz.lukynka.minestom.gamejam.utils.ActuallyGoodNodeFollower
import net.kyori.adventure.sound.Sound
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.network.packet.server.play.SoundEffectPacket
import net.minestom.server.scoreboard.Team
import net.minestom.server.sound.SoundEvent

abstract class AbstractEnemy(entityType: EntityType, elementType: ElementType?, val name: String) : EntityCreature(entityType) {

    val bindablePool = BindablePool()
    val elementType: Bindable<ElementType?> = bindablePool.provideBindable(elementType)
    val team: Bindable<Team?> = bindablePool.provideBindable(null)
    var canPathfind: Boolean = true

    abstract val sounds: Sounds
    abstract val damage: Float

    data class Sounds(
        val damageSound: SoundRange,
        val deathSound: SoundRange,
        val ambientSound: SoundRange,
    )

    data class SoundRange(val sound: SoundEvent, val pitchRange: ClosedFloatingPointRange<Float> = 1f..1f)

    fun playSound(sound: SoundRange, volume: Float = 0.8f) {
        this.playSound(sound.sound, volume, sound.pitchRange.random())
    }

    fun playSound(sound: SoundEvent, volume: Float, pitch: Float) {
        this.sendPacketToViewersAndSelf(SoundEffectPacket(sound, Sound.Source.HOSTILE, this.getPosition(), volume, pitch, 0))
    }

    override fun updateNewViewer(player: Player) {
        super.updateNewViewer(player)
        val packet = team.value?.createTeamsCreationPacket() ?: return
        player.sendPacket(packet)
    }

    init {
        team.valueChanged { event ->
            this.setTeam(event.newValue)
        }

        this.elementType.valueChanged { event ->
            if (event.newValue != null) {
                this[DataComponents.CUSTOM_NAME] = "${event.newValue!!.color}$name".miniMessage
                team.value = TeamManager.TEAMS[elementType]
            } else {
                this[DataComponents.CUSTOM_NAME] = "<white>$name".miniMessage
                team.value = null
            }
        }

        this.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.16

        team.triggerUpdate()
        this.elementType.triggerUpdate()
        this.isGlowing = true
        this.navigator.setNodeFollower { ActuallyGoodNodeFollower(this) }
    }
}