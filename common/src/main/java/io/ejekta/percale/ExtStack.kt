package io.ejekta.percale

import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.ItemStack

fun <T> ItemStack.edit(componentType: DataComponentType<T>, func: (curr: T?) -> T?) {
    val currentComponent = components[componentType]
    val newComponent = func(currentComponent)
    set(componentType, newComponent)
}

operator fun ItemStack.contains(componentType: DataComponentType<*>): Boolean {
    return components.has(componentType)
}

