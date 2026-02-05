package dom.builders

import dom.*

@ViewDsl
class CanvasBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<CanvasBuilder>(element, parentBuilder) {
    
    fun width(value: String): CanvasBuilder {
        element.attributes["width"] = value
        return this
    }

    override fun height(value: String) {
        element.attributes["height"] = value
    }
}
