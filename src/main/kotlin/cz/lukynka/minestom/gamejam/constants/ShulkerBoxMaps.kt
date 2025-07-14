package cz.lukynka.minestom.gamejam.constants

import cz.lukynka.minestom.gamejam.utils.shulkerMap
import cz.lukynka.shulkerbox.minestom.MapFileReader
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries

object ShulkerBoxMaps {
    val elevator = shulkerMap("elevator")
    val first = shulkerMap("map_first")
    val shop = shulkerMap("map_shop")

    val maps = Path("./shulkerbox/maps/")
        .listDirectoryEntries("*.shulker")
        .filter {
            it.fileName.startsWith("map_random_")
        }
        .map { path ->
            MapFileReader.read(path.toFile())
        }
}