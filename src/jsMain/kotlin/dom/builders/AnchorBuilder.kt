package dom.builders

import dom.*

@ViewDsl
class AnchorBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<AnchorBuilder>(element, parentBuilder) {
    
    fun href(value: String): AnchorBuilder {
        element.attributes["href"] = value
        return this
    }
}
