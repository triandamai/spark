package internal.devtools

import internal.BuildContext
import internal.Component
import kotlinx.browser.window
import org.w3c.dom.CustomEvent
import org.w3c.dom.CustomEventInit
import kotlin.js.json

object DevTools {
    private val components = mutableMapOf<String, Component>()

    var isEnabled = true

    fun registerComponent(id: String, component: Component) {
        components[id] = component
        dispatch("devtools-component-mount", json("id" to id, "name" to (component::class.simpleName ?: "Unknown")))
    }

    fun unregisterComponent(id: String) {
        components.remove(id)
        dispatch("devtools-component-unmount", json("id" to id))
    }

    fun logStateChange(componentId: String, idx: Int, stateValue: Any?) {
        val compName = components[componentId]?.let { it::class.simpleName } ?: componentId
        dispatch(
            "devtools-state-change", json(
                "id" to componentId,
                "idx" to idx,
                "name" to compName,
                "value" to stateValue.toString()
            )
        )
    }

    private fun dispatch(eventName: String, detail: Any?) {
        if (!isEnabled) return
        val init = CustomEventInit(detail = detail)
        val event = CustomEvent(eventName, init)
        try {
            window.dispatchEvent(event)
        } catch (e: Exception) {
            console.error(e)
        }
    }

    fun getComponent(id: String): Component? = components[id]
}
