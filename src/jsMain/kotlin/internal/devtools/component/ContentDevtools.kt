package internal.devtools.component

import internal.BuildContext
import internal.Component
import internal.View
import internal.builders.BaseElementBuilder
import internal.builders.DivBuilder

class ContentDevtools(
    private val selectedFeature: DevtoolFeature?,
    private val area: DivBuilder.()->Unit
): Component() {
    init {
        setSkipTracing(true)
    }
    override fun render(context: BuildContext): View = content{
        // Content Area
        div {
            className("flex-1 flex flex-col overflow-hidden bg-white")
            area()
        }
    }
}