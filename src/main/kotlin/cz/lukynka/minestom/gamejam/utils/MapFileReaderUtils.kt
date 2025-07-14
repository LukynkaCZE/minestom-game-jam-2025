package cz.lukynka.minestom.gamejam.utils

import cz.lukynka.shulkerbox.minestom.MapFileReader
import cz.lukynka.shulkerbox.minestom.MinestomShulkerboxMap
import java.io.File

fun shulkerMap(name: String): MinestomShulkerboxMap {
    return MapFileReader.read(
        File("./shulkerbox/maps/$name.shulker")
    )
}