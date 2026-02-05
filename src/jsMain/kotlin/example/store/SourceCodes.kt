package example.store

object SourceCodes {
    val bounced = """
class Bounced : Component() {
    private val x = state<Double>(window.innerWidth.toDouble())
    private val y = state(100.0)
    
    // ... physics logic with window.requestAnimationFrame ...
    
    override fun render(context: BuildContext): View {
        return content {
            div {
                // ...
                div {
                    className("absolute bg-orange-500 ...")
                    style("transform", "translate(${'$'}{x.value}px, ${'$'}{y.value}px)")
                    // ... basketball SVG ...
                }
            }
        }
    }
}
    """.trimIndent()

    val todo = """
package example

import dom.BuildContext
import dom.Component
import dom.View
import dom.types.DomEvent
import example.component.Input
import example.component.ItemTodoView
import example.store.ItemTodo
import example.store.todo
import kotlinx.browser.localStorage
import reactivity.removeAt
import reactivity.set

class Todo : Component() {
    val todoStore = useStore(todo)
    val items = state(listOf<ItemTodo>())
    
    val form = Input({
        items.value = items.value + ItemTodo(it)
    })

    override fun render(context: BuildContext): View {
        return content {
            div {
                className("min-h-screen bg-gradient-to-br from-purple-600 to-indigo-700 flex items-center justify-center py-10 px-4 font-sans")

                div {
                    className("max-w-xl w-full bg-white rounded-xl shadow-2xl overflow-hidden")
                    // ... header and form ...
                    items.eachIndexed { index, item ->
                        component {
                            ItemTodoView(
                                item.name,
                                isDone = item.done,
                                onDone = { done ->
                                    val modify = item.copy(done = done)
                                    items.set(index, modify)
                                },
                                onRemove = {
                                    items.removeAt(index)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
    """.trimIndent()

    val moveIt = """
class MoveIt : Component() {
    val translateX = state(0)
    val translateY = state(0)

    override fun render(context: BuildContext): View {
        return content {
            // ... sliders ...
            input {
                type(InputType.Range)
                bind(translateX)
                attr("min", "-100")
                attr("max", "100")
            }
            // ...
            div {
                className("w-32 h-32 bg-indigo-500 rounded-lg shadow-lg flex items-center justify-center text-white font-bold transition-transform duration-75 ease-out")
                style("transform", "translate(${'$'}{translateX.value}px, ${'$'}{translateY.value}px)")
                text("Move Me!")
            }
        }
    }
}
    """.trimIndent()

    val dragIt = """
class DragIt : Component() {
    val x = state(0)
    val y = state(0)
    val isDragging = state(false)
    
    // ... dragging logic ...
    
    override fun render(context: BuildContext): View {
        return content {
            div {
                // ...
                div {
                    className("absolute bg-rose-500 ...")
                    style("transform", "translate(${'$'}{x.value}px, ${'$'}{y.value}px)")
                    
                    on(DomEvent.MouseDown) { e ->
                        // start dragging
                    }
                }
                
                if (isDragging.value) {
                    div {
                        className("fixed inset-0 z-50 ...")
                        on(DomEvent.MouseMove) { e ->
                            // update x.value, y.value
                        }
                    }
                }
            }
        }
    }
}
    """.trimIndent()

    val useDirective = """
class TooltipDirective(private val text: String) : Directive {
    override fun update(element: Element) {
        element.setAttribute("title", text)
        (element as? HTMLElement)?.style?.cursor = "help"
    }
}

class UseDirective : Component() {
    val count = state(0)
    
    override fun render(context: BuildContext): View {
        return content {
            // ... auto-focus input ...
            input {
                use { el ->
                    (el as HTMLElement).focus()
                }
            }
            // ... custom manipulation ...
            div {
                use { el ->
                    val element = el as HTMLElement
                    val hue = (count.value * 20) % 360
                    element.style.backgroundColor = "hsl(${'$'}hue, 70%, 50%)"
                }
            }
        }
    }
}
    """.trimIndent()

    val transition = """
class Transition : Component() {
    val show = state(true)
    
    override fun render(context: BuildContext): View {
        return content {
            // ... fade transition ...
            if (show.value) {
                div {
                    transition(fade(500))
                    text("Fading Box")
                }
            }
            
            // ... fly transition in/out ...
            if (show.value) {
                div {
                    inTransition(fly(x = -100))
                    outTransition(fly(x = 100))
                    text("Flying Box")
                }
            }
        }
    }
}
    """.trimIndent()

    val profile = """
class Profile : Component() {
    override fun render(context: BuildContext): View {
        return content {
            div {
                // ...
                h2 {
                    val name = query<String>("name") ?: "Guest User"
                    text(name)
                }
                p {
                    text("ID: #${'$'}{param<Any>("id")}")
                }
            }
        }
    }
}
    """.trimIndent()

    val canvas = """
class CanvasDemo : Component() {
    override fun render(context: BuildContext): View {
        return content {
            canvas {
                width("600")
                height("400")
                className("border border-gray-200 rounded-lg shadow-inner bg-white")
                use { el ->
                    val canvas = el as HTMLCanvasElement
                    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
                    
                    // Simple Drawing
                    ctx.fillStyle = "rgb(79, 70, 229)" // Indigo-600
                    ctx.fillRect(50.0, 50.0, 150.0, 150.0)
                    
                    ctx.strokeStyle = "rgb(225, 29, 72)" // Rose-600
                    ctx.lineWidth = 5.0
                    ctx.strokeRect(250.0, 50.0, 150.0, 150.0)
                }
            }
        }
    }
}
    """.trimIndent()
}