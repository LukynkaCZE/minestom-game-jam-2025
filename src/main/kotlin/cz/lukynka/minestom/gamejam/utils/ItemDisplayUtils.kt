package cz.lukynka.minestom.gamejam.utils

import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.entity.metadata.display.TextDisplayMeta
import net.minestom.server.instance.Instance
import java.util.concurrent.CompletableFuture

/**
 * Returns [Entity] of type `ITEM_DISPLAY_ENTITY`
 */
inline fun spawnItemDisplay(world: Instance, position: Point, block: ItemDisplayMeta.() -> Unit): CompletableFuture<Entity> {
    val entity = Entity(EntityType.ITEM_DISPLAY)
    entity.setNoGravity(true)
    (entity.entityMeta as ItemDisplayMeta).apply(block)
    return entity.setInstance(world, position)
        .thenApply { entity }
}

inline fun spawnTextDisplay(world: Instance, position: Point, block: TextDisplayMeta.() -> Unit): Entity {
    val entity = Entity(EntityType.TEXT_DISPLAY)
    entity.setNoGravity(true)
    (entity.entityMeta as TextDisplayMeta).apply(block)
    entity.setInstance(world, position)
    return entity
}
