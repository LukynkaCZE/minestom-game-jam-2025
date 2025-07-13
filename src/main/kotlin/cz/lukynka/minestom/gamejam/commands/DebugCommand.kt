package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.entity.Zombie
import cz.lukynka.minestom.gamejam.isAdmin
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.display.ItemDisplayMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

object DebugCommand : Command("debug") {
    init {
        setCondition { sender, _ ->
            sender is Player && sender.isAdmin()
        }

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            val pos = sender.position
            val world = sender.instance!!

            val zombie = Zombie()
            zombie.setInstance(world, pos)
        }, ArgumentType.Literal("spawn_zombie"))

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            val en = Entity(EntityType.ITEM_DISPLAY)
            en.setNoGravity(true)
            val meta = en.entityMeta as ItemDisplayMeta
            meta.itemStack = ItemStack.of(Material.values().random())
            en.setInstance(sender.instance, sender.position.withView(0f, 0f))
        }, ArgumentType.Literal("spawn_display_entity"))
    }
}