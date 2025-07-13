package cz.lukynka.minestom.gamejam.commands

import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

object GiveCommand : Command("give") {
    init {
        setCondition { sender, _ ->
            sender is Player && sender.permissionLevel >= 4
        }

        val itemArg = ArgumentType.ItemStack("item")

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            sender.inventory.addItemStack(context.get(itemArg))
        }, itemArg)
    }
}