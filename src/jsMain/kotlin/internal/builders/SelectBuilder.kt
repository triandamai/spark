package internal.builders

import internal.*
import reactivity.State

@ViewDsl
class SelectBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<SelectBuilder>(element, parentBuilder) {
    
    fun multiple(value: Boolean = true): SelectBuilder {
        if (value) {
            element.attributes["multiple"] = "multiple"
        } else {
            element.attributes.remove("multiple")
        }
        return this
    }

    fun bind(state: State<String>): SelectBuilder {
        element.attributes["value"] = state.value
        element.listeners["change"] = { event ->
            val target = event.target as? org.w3c.dom.HTMLSelectElement
            if (target != null) {
                state.value = target.value
            }
        }
        return this
    }

    fun option(block: OptionBuilder.() -> Unit) {
        val el = VElement("option")
        val builder = OptionBuilder(el, null)
        builder.block()
        element.children.add(el)
    }
}
