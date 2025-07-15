@file:Suppress("UnstableApiUsage")

package cz.lukynka.minestom.gamejam.constants

import net.minestom.server.component.DataComponentMap
import net.minestom.server.component.DataComponents
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.CustomModelData

object ItemStackConstants {
    val SHAFT = ItemStack.of(
        Material.STICK,
        DataComponentMap.builder()
            .set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(listOf(9f), listOf(), listOf(), listOf()))
            .build()
    )
}
