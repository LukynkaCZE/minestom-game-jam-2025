@file:Suppress("UnstableApiUsage")

package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.combat.ElementType
import cz.lukynka.minestom.gamejam.constants.ShulkerBoxMaps
import cz.lukynka.minestom.gamejam.constants.StyleConstants.GREY_69
import cz.lukynka.minestom.gamejam.entity.Zombie
import cz.lukynka.minestom.gamejam.extensions.round
import cz.lukynka.minestom.gamejam.extensions.sendPacket
import cz.lukynka.minestom.gamejam.utils.spawnItemDisplay
import cz.lukynka.minestom.gamejam.world2GameInstanceMap
import cz.lukynka.shulkerbox.minestom.MapFileReader
import cz.lukynka.shulkerbox.minestom.conversion.toMinestomMap
import cz.lukynka.shulkerbox.minestom.versioncontrol.GitIntegration
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.color.Color
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.component.DataComponentMap
import net.minestom.server.component.DataComponents
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.projectile.FireworkRocketMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.CustomModelData
import net.minestom.server.item.component.FireworkExplosion
import net.minestom.server.item.component.FireworkList
import net.minestom.server.network.packet.server.play.EntityStatusPacket
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

            val zombie = Zombie(ElementType.entries.random())
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

        addSyntax({ sender, context ->
            val player = sender as Player

            val firework = Entity(EntityType.FIREWORK_ROCKET)
            val meta = firework.entityMeta as FireworkRocketMeta
            meta.fireworkInfo = ItemStack.builder(Material.FIREWORK_ROCKET).set(DataComponents.FIREWORKS, FireworkList(1, listOf(FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, listOf(Color(255, 255, 255)), listOf(), true, true)))).build()

            firework.setNoGravity(true)
            firework.setInstance(player.instance).thenAccept {
                firework.teleport(player.position)
            }

        }, ArgumentType.Literal("firework"))

        addSubcommand(Shulkerbox)
        addSubcommand(GameCommand)
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
                                        GREY_69
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

    object GameCommand : Command("game") {
        init {
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax

                val game = world2GameInstanceMap[sender.instance]
                game?.dispose()
            }, ArgumentType.Literal("dispose"))

            val mapArg = ArgumentType.String("map")
            mapArg.setSuggestionCallback { sender, context, suggestion ->
                ShulkerBoxMaps.maps.forEach {
                    suggestion.addEntry(
                        SuggestionEntry(it.map.name)
                    )
                }
            }

            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax

                val game = world2GameInstanceMap[sender.instance] ?: return@addSyntax

                val name = context.get(mapArg)
                ShulkerBoxMaps.maps.find {
                    it.map.name == name
                }?.let(game::spawnMap)
            }, ArgumentType.Literal("place"), mapArg)
        }
    }
}