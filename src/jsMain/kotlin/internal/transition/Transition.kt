package internal.transition

interface Transition {
    fun start(element: org.w3c.dom.Element, onComplete: () -> Unit)
}