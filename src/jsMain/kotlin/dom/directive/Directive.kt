package dom.directive

interface Directive {
    fun update(element: org.w3c.dom.Element) {}
    fun destroy(element: org.w3c.dom.Element) {}
}

fun interface DirectiveAction {
    fun update(element: org.w3c.dom.Element)
}