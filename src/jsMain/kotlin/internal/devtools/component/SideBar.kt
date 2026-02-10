package internal.devtools.component

import internal.BuildContext
import internal.Component
import internal.View

data class DevtoolFeature(
    val name: String,
    val description: String,
    val id: String
)

class SideBar(
    var features: List<DevtoolFeature>,
    var selected: DevtoolFeature?,
    var onSelected:(DevtoolFeature)->Unit
) : Component() {
    init {
        setSkipTracing(true)
    }
    override fun render(context: BuildContext): View {
        return content {
            div {
                className("w-64 bg-gray-50 border-r border-gray-200 flex flex-col")
                div {
                    className("p-2 font-semibold text-gray-500 uppercase text-[10px] tracking-wider")
                    text("Features")
                }
                features.each {it->
                    element {
                        // Feature List
                        div {
                            className("px-2")
                            div {
                                className("p-2 rounded cursor-pointer transition-colors ${if (selected?.id == it.id) "bg-blue-100 text-blue-700 font-medium" else "hover:bg-gray-100 text-gray-600"}")
                                text(it.name)
                                on("click") {ev->
                                    onSelected(it)
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}