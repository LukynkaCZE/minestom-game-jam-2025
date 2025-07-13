@file:Suppress("UnstableApiUsage")

package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.entity.Zombie
import cz.lukynka.minestom.gamejam.extensions.round
import cz.lukynka.minestom.gamejam.utils.spawnItemDisplay
import cz.lukynka.shulkerbox.minestom.MapFileReader
import cz.lukynka.shulkerbox.minestom.conversion.toMinestomMap
import cz.lukynka.shulkerbox.minestom.versioncontrol.GitIntegration
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.component.DataComponentMap
import net.minestom.server.component.DataComponents
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.CustomModelData
import kotlin.io.path.*

object DebugCommand : Command("debug") {
    init {
        setCondition { sender, _ ->
            sender is Player && sender.permissionLevel >= 4
        }

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            val pos = sender.position
            val world = sender.instance!!

            val zombie = Zombie()
            zombie.setInstance(world, pos)
        }, ArgumentType.Literal("spawn_zombie"))

        val itemArg = ArgumentType.ItemStack("item")

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            spawnItemDisplay(
                sender.instance,
                sender.position.withView(0f, 0f)
            ) {
                itemStack = context.get(itemArg)
            }
        }, ArgumentType.Literal("spawn_display_entity"), itemArg)

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            spawnItemDisplay(
                sender.instance,
                sender.position
                    .withView(0f, 0f)
                    .add(-8.5, 7.0, -8.5)
            ) {
                itemStack = ItemStack.of(
                    Material.STICK,
                    DataComponentMap.builder()
                        .set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(listOf(9f), listOf(), listOf(), listOf()))
                        .build()
                )
                scale = Vec(32.0)
            }
        }, ArgumentType.Literal("spawn_elevator_model"))

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            spawnItemDisplay(
                sender.instance,
                sender.position.withView(0f, 0f)
            ) {
                itemStack = ItemStack.of(Material.values().random())
            }
        }, ArgumentType.Literal("spawn_display_entity"))

        addSubcommand(Shulkerbox)
    }

    object Shulkerbox : Command("shulkerbox") {
        init {
            addSyntax({ sender, context ->
                GitIntegration.pull()
                sender.sendMessage("Pulled")
            }, ArgumentType.Literal("pull"))

            val mapArgument = ArgumentType.String("map")
            mapArgument.setSuggestionCallback { sender, context, suggestion ->
                val directory = Path("./shulkerbox/maps/")
                if (!directory.isDirectory()) {
                    return@setSuggestionCallback
                }

                directory.forEachDirectoryEntry("*.shulker") { path ->
                    suggestion.addEntry(
                        SuggestionEntry(path.pathString)
                    )
                }
            }
            mapArgument.setCallback({ sender, _ ->
                sender.sendMessage("invalid map!")
            })

            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax

                val path = Path(context.get(mapArgument))
                if (!path.exists()) {
                    sender.sendMessage("Map doesn't exist")
                    return@addSyntax
                }

                val map = MapFileReader.read(path.toFile())
                        .toMinestomMap(sender.position.round(), sender.instance)
                map.placeSchematicAsync()
                    .thenRun {
                        sender.teleport(Pos(map.getPoint("spawn").location))
                        sender.sendMessage(
                            Component.text("Placed ")
                                .append(
                                    Component.text(
                                        path.toString(),
                                        TextColor.color(0x696969)
                                    )
                                )
                                .append(Component.text("!"))
                                .appendNewline()
                                .append(Component.text("Teleported you to spawn!"))
                        )
                    }
            }, ArgumentType.Literal("place"), mapArgument)
        }
    }
}