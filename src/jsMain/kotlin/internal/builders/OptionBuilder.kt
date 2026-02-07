package internal.builders

import internal.*

@ViewDsl
class OptionBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<OptionBuilder>(element, parentBuilder) {
    
    fun value(value: String): OptionBuilder {
        element.attributes["value"] = value
        return this
    }

    fun selected(value: Boolean = true): OptionBuilder {
        if (value) {
            element.attributes["selected"] = "selected"
        } else {
            element.attributes.remove("selected")
        }
        return this
    }
}
