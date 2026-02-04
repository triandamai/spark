package example.component

import dom.BuildContext
import dom.Component
import dom.View
import dom.types.DomEvent

class ItemTodoView(
    private var todo: String,
    private var isDone: Boolean = false,
    private var onDone: (Boolean) -> Unit,
    private var onRemove: () -> Unit
) : Component() {

    override fun update(other: Component) {
        super.update(other)
        console.log("updated ItemTodoView")
    }
    val name = state("")

    override fun onMounted() {
        super.onMounted()
        console.log("onMounted for " + "${this.hashCode()}")
    }

    override fun onUnmounted() {
        super.onUnmounted()
        console.log("onUnmounted for " + "${this.hashCode()}")
    }

    override fun render(context: BuildContext): View {
        return content {
            div {
                className("group flex items-center justify-between p-4 mb-3 bg-white border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-all duration-200")
                
                div {
                    className("flex items-center flex-1 cursor-pointer")
                    on(DomEvent.Click) {
                        onDone(!isDone)
                    }

                    div {
                        val baseCheck = "h-5 w-5 rounded border border-gray-300 mr-3 flex items-center justify-center transition-colors duration-200"
                        className(if (isDone) "$baseCheck bg-green-500 border-green-500" else "$baseCheck bg-white")
                        if (isDone) {
                             rawHtml("""<svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg>""")
                        }
                    }

                    div {
                        className(if (isDone) "text-gray-400 line-through transition-all duration-200" else "text-gray-800 font-medium transition-all duration-200")
                        text(todo)
                    }
                }

                button {
                    className("text-gray-400 hover:text-red-500 focus:outline-none transition-colors duration-200 p-2 rounded-full hover:bg-red-50 ml-4 opacity-0 group-hover:opacity-100")
                    on(DomEvent.Click) {
                        onRemove.invoke()
                    }
                    rawHtml("""<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>""")
                }
            }
        }
    }

}