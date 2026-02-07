package example.component

import internal.directive.Directive
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

class TooltipDirective(private val text: String) : Directive {
    override fun update(element: Element) {
        element.setAttribute("title", text)
        (element as? HTMLElement)?.style?.cursor = "help"
    }
}