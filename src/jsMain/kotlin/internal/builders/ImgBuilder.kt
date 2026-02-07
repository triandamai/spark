package internal.builders

import internal.*

@ViewDsl
class ImgBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<ImgBuilder>(element, parentBuilder) {
    
    fun src(value: String): ImgBuilder {
        element.attributes["src"] = value
        return this
    }

    fun alt(value: String): ImgBuilder {
        element.attributes["alt"] = value
        return this
    }

    fun width(value: Int): ImgBuilder {
        element.attributes["width"] = value.toString()
        return this
    }

    fun height(value: Int): ImgBuilder {
        element.attributes["height"] = value.toString()
        return this
    }
}
