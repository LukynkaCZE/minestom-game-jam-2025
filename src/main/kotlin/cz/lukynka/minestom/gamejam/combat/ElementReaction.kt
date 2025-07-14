package cz.lukynka.minestom.gamejam.combat

enum class ElementReaction(val first: ElementType, val second: ElementType, val displayName: String, val color: String) {
    CONDUCTIVE(ElementType.ELECTRICAL, ElementType.WATER, "Conductive", "<#8b60f7>"),
    OVERCHARGED(ElementType.ELECTRICAL, ElementType.FIRE, "Overcharged", "<#ff1499>"),
    FROZEN(ElementType.WATER, ElementType.ICE, "Frozen", "<#a6d2ff>"),
    STEAM(ElementType.WATER, ElementType.FIRE, "Steam", "<#fff8e3>"),
    HYPERMELT(ElementType.ICE, ElementType.FIRE, "Hypermelt", "<#ff9e61>"),
    SUPERCONDUCT(ElementType.ICE, ElementType.ELECTRICAL, "Superconduct", "<#6161ff>"),
}

fun getElementReaction(first: ElementType, second: ElementType): ElementReaction? {
    return ElementReaction.entries.firstOrNull {
        (it.first == first && it.second == second) || (it.first == second && it.second == first)
    }
}
