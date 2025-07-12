package cz.lukynka.minestom.gamejam

import cz.lukynka.bindables.BindablePool
import cz.lukynka.minestom.gamejam.event.EventPool
import cz.lukynka.minestom.gamejam.extensions.location
import cz.lukynka.minestom.gamejam.extensions.toLocation
import cz.lukynka.minestom.gamejam.types.Location
import cz.lukynka.minestom.gamejam.vectors.Vector3
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.instance.InstanceTickEvent
import net.minestom.server.instance.block.Block

class Bound(first: Location, second: Location) : Disposable {

    private val bindablePool = BindablePool()
    private val eventPool = EventPool()

    var firstLocation = first
        internal set

    var secondLocation = second
        internal set

    val world get() = firstLocation.world
    val size: Vector3 get() = firstLocation.distanceVector(secondLocation).toVector3()

    private val members: MutableList<Player> = mutableListOf()
    val players: List<Player> get() = members.toList()

    val onEnter = bindablePool.provideBindableDispatcher<Player>()
    val onLeave = bindablePool.provideBindableDispatcher<Player>()

    val highestPoint: Location
        get() {
            val maxX = maxOf(firstLocation.x, secondLocation.x)
            val maxY = maxOf(firstLocation.y, secondLocation.y)
            val maxZ = maxOf(firstLocation.z, secondLocation.z)

            return Location(maxX, maxY, maxZ, world)
        }

    val lowestPoint: Location
        get() {
            val minX = minOf(firstLocation.x, secondLocation.x)
            val minY = minOf(firstLocation.y, secondLocation.y)
            val minZ = minOf(firstLocation.z, secondLocation.z)

            return Location(minX, minY, minZ, world)
        }

    init {
        resize(firstLocation, secondLocation)
        eventPool.on<InstanceTickEvent> { event ->
            if (event.instance != world) return@on

            event.instance.players.forEach { player ->
                if (player.instance != world) return@on
                if (player.location.isWithinBound(this) && !members.contains(player)) {
                    members.add(player)
                    onEnter.dispatch(player)
                } else if (!player.location.isWithinBound(this) && members.contains(player)) {
                    members.remove(player)
                    onLeave.dispatch(player)
                }
            }
        }
    }

    fun resize(newFirstLocation: Location, newSecondLocation: Location) {
        if (newFirstLocation.world != newSecondLocation.world) throw IllegalArgumentException("The two locations cannot be in different worlds (${firstLocation.world.dimensionName} - ${secondLocation.world.dimensionName})")
        this.firstLocation = getBoundPositionRelative(newFirstLocation, newSecondLocation)
        this.secondLocation = getBoundPositionRelative(newSecondLocation, newFirstLocation)

        getEntities().filterIsInstance<Player>().forEach { player ->
            if (members.contains(player)) return@forEach
            members.add(player)
            onEnter.dispatch(player)
        }
    }

    fun getBlocks(): Map<Location, Block> {
        val allBlocks = mutableMapOf<Location, Block>()
        val first = firstLocation
        val second = secondLocation

        val minX = minOf(first.x, second.x)
        val maxX = maxOf(first.x, second.x)
        val minY = minOf(first.y, second.y)
        val maxY = maxOf(first.y, second.y)
        val minZ = minOf(first.z, second.z)
        val maxZ = maxOf(first.z, second.z)

        for (iX in minX.toInt()..maxX.toInt()) {
            for (iY in minY.toInt()..maxY.toInt()) {
                for (iZ in minZ.toInt()..maxZ.toInt()) {
                    val location = Location(iX, iY, iZ, world)
                    allBlocks[location] = world.getBlock(iX, iY, iZ)
                }
            }
        }
        return allBlocks.toMap()
    }

    private fun getBoundPositionRelative(first: Location, second: Location): Location {
        var finalX = first.x
        var finalY = first.y
        var finalZ = first.z

        if (first.x > second.x) finalX = first.x + 0.99999
        if (first.y > second.y) finalY = first.y + 0.99999
        if (first.z > second.z) finalZ = first.z + 0.99999

        return Location(finalX, finalY, finalZ, first.world)
    }

    fun getEntities(): List<Entity> {
        val entities = mutableListOf<Entity>()

        world.entities.toList().forEach { entity -> if (entity.position.toLocation(world).isWithinBound(this)) entities.add(entity) }
        return entities
    }

    override fun dispose() {
        bindablePool.dispose()
        eventPool.dispose()
        members.clear()
    }
}