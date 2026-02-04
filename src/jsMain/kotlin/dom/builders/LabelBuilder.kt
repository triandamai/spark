package dom.builders

import dom.*

@ViewDsl
class LabelBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<LabelBuilder>(element, parentBuilder) {
    
    fun forId(value: String): LabelBuilder {
        element.attributes["for"] = value
        return this
    }
}
