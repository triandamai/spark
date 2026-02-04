package example

import dom.Component
import dom.BuildContext
import dom.Route
import dom.Router.Companion.navigate
import dom.View
import dom.types.DomEvent

class Home : Component() {
    val count = state(0)

    override fun render(context: BuildContext): View {
        return content {
            div {
                classname("flex flex-col justify-center align-center items-center w-full h-full bg-gray-200")
                a("/") {
                    text("Go to about")
                }
                button {
                    on(DomEvent.Click) {
                        navigate("/profile/1?name=Juan")
                    }
                    text("Go to profile")
                }
                p {
                    text("Hello World! from home")
                }
                div {
                    classname("flex flex-row items-center w-full h-full bg-gray-200")
                    button {
                        classname("mx-2 bg-green-100 rounded-md px-10 py-4")
                        text("Increment")
                        on(DomEvent.Click) {
                            count.value++
                        }
                    }
                    p {
                        text("Counter: ${count()}")
                    }
                    button {
                        classname("mx-2 bg-green-100 rounded-md px-10 py-4")
                        text("Decrement")
                        on(DomEvent.Click) {
                            count.value--
                        }
                    }
                }
            }
        }
    }

}