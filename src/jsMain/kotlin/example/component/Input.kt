package example.component

import internal.BuildContext
import internal.Component
import internal.View
import internal.types.DomEvent
import internal.types.InputType

class Input(
    private var onSubmit: (String) -> Unit
) : Component() {
    val name = state("")
    override fun onMounted() {
        super.onMounted()
        console.log("efect on input")
    }

    override fun render(context: BuildContext): View = content {
        div {
            className("w-full px-10 py-10 bg-gray-100")
            div {
                className("w-full flex flex-row")
                input {
                    className("w-1/2 rounded-md border-gray-100 border border-2 px-2 py-4")
                    placeholder("Hello world!")
                    type(InputType.Text)
                    bind(name)
                    on(DomEvent.KeyDown) {
                        if (it.asDynamic().code == "Enter") {
                            onSubmit(name.value)
                            name.value = ""
                        }
                    }
                }
                button {
                    className("bg-green-200 ml-10 rounded-md px-10 py-4")
                    text("Add")
                    on(DomEvent.Click) { event ->
                        onSubmit(name.value)
                        name.value = ""
                    }
                }
            }
            div {
                className("underline")
                text("Typing ${name.value}")
            }
        }
    }
}