package dom.builders

import dom.ElementBuilder
import dom.ViewDsl
import dom.VElement
import reactivity.State

@ViewDsl
class TextAreaBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<TextAreaBuilder>(element, parentBuilder) {
    
    fun rows(value: Int): TextAreaBuilder {
        element.attributes["rows"] = value.toString()
        return this
    }

    fun cols(value: Int): TextAreaBuilder {
        element.attributes["cols"] = value.toString()
        return this
    }

    fun placeholder(value: String): TextAreaBuilder {
        element.attributes["placeholder"] = value
        return this
    }

    fun value(value: String): TextAreaBuilder {
        element.attributes["value"] = value
        return this
    }

    fun bind(state: State<String>): TextAreaBuilder {
        element.attributes["value"] = state.value
        element.listeners["input"] = { event ->
            val target = event.target as? org.w3c.dom.HTMLTextAreaElement
            if (target != null) {
                state.value = target.value
            }
        }
        return this
    }
}
