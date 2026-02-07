package internal.builders

import internal.*
import internal.types.InputType
import reactivity.State

@ViewDsl
class InputBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<InputBuilder>(element, parentBuilder) {
    
    /**
     * Set the input type using type-safe InputType.
     * Example: type(InputType.Email)
     */
    fun type(value: InputType): InputBuilder {
        element.attributes["type"] = value.value
        return this
    }
    
    /**
     * Set the input type using a string (for backward compatibility).
     * Example: type("text")
     */
    fun type(value: String): InputBuilder {
        element.attributes["type"] = value
        return this
    }

    fun placeholder(value: String): InputBuilder {
        element.attributes["placeholder"] = value
        return this
    }

    fun value(value: String): InputBuilder {
        element.attributes["value"] = value
        return this
    }

    fun bind(state: State<String>): InputBuilder {
        element.attributes["value"] = state.value
        element.listeners["input"] = { event ->
            val target = event.target as? org.w3c.dom.HTMLInputElement
            if (target != null) {
                state(target.value)
            }
        }
        return this
    }

    fun bind(state: State<Int>): InputBuilder {
        element.attributes["value"] = state.value.toString()
        element.listeners["input"] = { event ->
            val target = event.target as? org.w3c.dom.HTMLInputElement
            if (target != null) {
                state(target.value.toIntOrNull() ?: 0)
            }
        }
        return this
    }

    /**
     * Convenience method to register a change event listener.
     * Equivalent to: on(DomEvent.Change) { event -> ... }
     * 
     * Example:
     * ```
     * input {
     *     type(InputType.Text)
     *     onChange { event ->
     *         println("Value changed!")
     *     }
     * }
     * ```
     */
    fun onChange(listener: (org.w3c.dom.events.Event) -> Unit): InputBuilder {
        element.listeners["change"] = listener
        return this
    }
}
