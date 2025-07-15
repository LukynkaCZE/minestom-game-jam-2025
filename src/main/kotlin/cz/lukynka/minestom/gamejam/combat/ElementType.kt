package cz.lukynka.minestom.gamejam.combat

import cz.lukynka.minestom.gamejam.vectors.Vector3
import net.kyori.adventure.text.format.NamedTextColor

enum class ElementType(val displayName: String, val color: String, val rgb: Vector3, val icon: String, val glowColor: NamedTextColor) {
    ELECTRICAL("Electrical", "<#ff55ff>", Vector3(), "", NamedTextColor.LIGHT_PURPLE),
    WATER("Water", "<#00aaaa>", Vector3(), "", NamedTextColor.DARK_AQUA),
    ICE("Ice", "<#55ffff>", Vector3(), "", NamedTextColor.AQUA),
    FIRE("Fire", "<#ff5555>", Vector3(), "", NamedTextColor.RED),
}