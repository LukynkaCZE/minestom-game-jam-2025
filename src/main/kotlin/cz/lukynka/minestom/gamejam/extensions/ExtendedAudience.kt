package cz.lukynka.minestom.gamejam.extensions

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

fun Audience.sendMessage(msg: String) = sendMessage(Component.text(msg))
