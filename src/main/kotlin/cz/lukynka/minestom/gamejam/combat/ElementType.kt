package cz.lukynka.minestom.gamejam.combat

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class ElementType(val displayName: String, val color: String, val icon: String, val glowColor: TextColor) {
    ELECTRICAL("Electrical", "<#ff55ff>", "", NamedTextColor.LIGHT_PURPLE),
    WATER("Water", "<#00aaaa>", "", NamedTextColor.DARK_AQUA),
    ICE("Ice", "<#55ffff>", "", NamedTextColor.AQUA),
    FIRE("Fire", "<#ff5555>", "", NamedTextColor.RED),
}