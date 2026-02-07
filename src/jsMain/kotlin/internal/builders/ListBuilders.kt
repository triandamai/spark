package internal.builders

import internal.*

// List builders
@ViewDsl
class UlBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<UlBuilder>(element, parentBuilder) {
    
    fun li(block: LiBuilder.() -> Unit) {
        val el = VElement("li")
        val builder = LiBuilder(el, null)
        builder.block()
        element.children.add(el)
    }
}

@ViewDsl
class OlBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<OlBuilder>(element, parentBuilder) {
    
    fun li(block: LiBuilder.() -> Unit) {
        val el = VElement("li")
        val builder = LiBuilder(el, null)
        builder.block()
        element.children.add(el)
    }
}

@ViewDsl
class LiBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<LiBuilder>(element, parentBuilder) {
}
