package dom.transition

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlinx.browser.window

/**
 * More robust transitions using Svelte-like approach where we can explicitly define IN and OUT.
 */
abstract class BaseTransition(val duration: Int = 300) : Transition {
    var mode: TransitionMode = TransitionMode.IN

    enum class TransitionMode { IN, OUT }

    fun asIn(): Transition {
        val t = this
        return object : Transition {
            override fun start(element: Element, onComplete: () -> Unit) {
                t.mode = TransitionMode.IN
                t.start(element, onComplete)
            }
        }
    }

    fun asOut(): Transition {
        val t = this
        return object : Transition {
            override fun start(element: Element, onComplete: () -> Unit) {
                t.mode = TransitionMode.OUT
                t.start(element, onComplete)
            }
        }
    }
}

class Fade(duration: Int = 300) : BaseTransition(duration) {
    override fun start(element: Element, onComplete: () -> Unit) {
        val htmlElement = element as? HTMLElement ?: return
        htmlElement.style.transition = "opacity ${duration}ms"
        
        if (mode == TransitionMode.OUT) {
            htmlElement.style.opacity = "1"
            window.requestAnimationFrame {
                window.requestAnimationFrame {
                    htmlElement.style.opacity = "0"
                }
            }
            window.setTimeout({ onComplete() }, duration)
        } else {
            htmlElement.style.opacity = "0"
            window.requestAnimationFrame {
                window.requestAnimationFrame {
                    htmlElement.style.opacity = "1"
                }
            }
            window.setTimeout({ onComplete() }, duration)
        }
    }
}

class Fly(val x: Int = 0, val y: Int = 0, duration: Int = 300) : BaseTransition(duration) {
    override fun start(element: Element, onComplete: () -> Unit) {
        val htmlElement = element as? HTMLElement ?: return
        htmlElement.style.transition = "opacity ${duration}ms, transform ${duration}ms"
        
        if (mode == TransitionMode.OUT) {
            htmlElement.style.opacity = "1"
            htmlElement.style.transform = "translate(0px, 0px)"
            window.requestAnimationFrame {
                window.requestAnimationFrame {
                    htmlElement.style.opacity = "0"
                    htmlElement.style.transform = "translate(${x}px, ${y}px)"
                }
            }
            window.setTimeout({ onComplete() }, duration)
        } else {
            htmlElement.style.opacity = "0"
            htmlElement.style.transform = "translate(${x}px, ${y}px)"
            window.requestAnimationFrame {
                window.requestAnimationFrame {
                    htmlElement.style.opacity = "1"
                    htmlElement.style.transform = "translate(0px, 0px)"
                }
            }
            window.setTimeout({ onComplete() }, duration)
        }
    }
}

fun fade(duration: Int = 300) = Fade(duration)
fun fly(x: Int = 0, y: Int = 0, duration: Int = 300) = Fly(x, y, duration)
