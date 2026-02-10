package internal.devtools.pages

import internal.BuildContext
import internal.Component
import internal.View

class ComponentTracing: Component() {
    init {
        setSkipTracing(true)
    }
    override fun render(context: BuildContext): View = content {

    }
}