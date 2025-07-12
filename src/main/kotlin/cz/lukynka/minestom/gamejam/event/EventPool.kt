package cz.lukynka.minestom.gamejam.event

import cz.lukynka.minestom.gamejam.Disposable
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minestom.server.event.Event
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import java.util.*
import kotlin.reflect.KClass

class EventPool : Disposable {
    val filter: EventFilter<*, *> = EventFilter.ALL
    val eventMap = Object2ObjectOpenHashMap<KClass<out Event>, EventNode<*>>()
    val eventNode = EventNode.all("event-pool-${UUID.randomUUID()}")

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Event> on(noinline function: EventListenerFunction<T>): EventNode<T> {
        val eventType = T::class.java
        val node = eventNode.addListener(eventType) { event ->
            function.invoke(event)
        }
        eventMap[T::class] = node
        return node as EventNode<T>
    }

    inline fun <reified T : Event> unregister(listener: EventNode<*>) {
        eventMap.remove(T::class)
        eventNode.removeChild(listener)
    }

    override fun dispose() {
        eventMap.forEach { (clazz, node) ->
            eventMap.remove(clazz)
            eventNode.removeChild(node)
        }
        eventMap.clear()
    }
}

typealias EventListenerFunction<T> = (event: T) -> Unit