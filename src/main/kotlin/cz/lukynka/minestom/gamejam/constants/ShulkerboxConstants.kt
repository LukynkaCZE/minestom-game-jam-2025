package cz.lukynka.minestom.gamejam.constants

import cz.lukynka.minestom.gamejam.utils.shulkerMap
import cz.lukynka.shulkerbox.minestom.MapFileReader
import net.minestom.server.coordinate.Vec
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString

object ShulkerBoxMaps {
    val FIRST_MAP_OFFSET = Vec(0.0, 2.0, 3.0)

    val elevator = shulkerMap("elevator")
    val first = shulkerMap("map_first")
    val shop = shulkerMap("map_shop")

    val maps = Path("./shulkerbox/maps/")
        .listDirectoryEntries("*.shulker")
        .filter {
            it.fileName.pathString.startsWith("map_random_")
        }
        .map { path ->
            MapFileReader.read(path.toFile())
        }
}

object ShulkerboxPointConstants {
    const val SPAWN = "spawn"
    const val BLAST_DOOR = "blast_door"
    const val MOB_SPAWN = "mob_spawn"
    const val NEXT_LEVEL_PICKER = "next_level_picker"

}

object ShulkerboxBounds {
    const val NEXT_LEVEL_DOOR = "next_level_door"
    const val GATE = "gate"
}
