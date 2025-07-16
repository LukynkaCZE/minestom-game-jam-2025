package cz.lukynka.minestom.gamejam.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

object MiniMessageHelper {
    private val instance = MiniMessage.builder().build()

    fun translate(string: String): Component {
        return instance.deserialize(string)
    }
}

val String.miniMessage: Component get() = MiniMessageHelper.translate(this.replace("<lime>", "<green>"))