package cz.lukynka.minestom.gamejam.utils

import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.instance.Instance

/**
 * Returns [Entity] of type `ITEM_DISPLAY_ENTITY`
 */
inline fun spawnItemDisplay(world: Instance, position: Point, block: ItemDisplayMeta.() -> Unit): Entity {
    val entity = Entity(EntityType.ITEM_DISPLAY)
    entity.setNoGravity(true)
    (entity.entityMeta as ItemDisplayMeta).apply(block)
    entity.setInstance(world, position)
    return entity
}
