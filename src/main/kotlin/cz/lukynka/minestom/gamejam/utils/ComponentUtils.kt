package cz.lukynka.minestom.gamejam.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

@Suppress("UNCHECKED_CAST")
fun clickableCommand(command: String): Component {
    return Component.text(
        command,
        Style.style()
            .clickEvent(
                ClickEvent.suggestCommand(command)
            )
            .hoverEvent { it -> Component.text("Click to run command").asHoverEvent() as HoverEvent<Any> }
            .decorate(TextDecoration.UNDERLINED)
            .build()
    )
}