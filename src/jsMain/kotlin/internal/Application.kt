package internal

import internal.devtools.DevToolsUI
import org.w3c.dom.Element

object Application {
    fun mount(root: Element?, router: Router): BuildContext {
        if (root == null) {
            throw RuntimeException("Cannot mount to a null root element")
        }

        // Initialize DevTools
        val devToolsRoot = kotlinx.browser.document.createElement("div")
        kotlinx.browser.document.body?.appendChild(devToolsRoot)
        mount(devToolsRoot, DevToolsUI(), isMain = false)

        val context = BuildContext()
        BuildContext.root = context
        router.mount(el = root, context = context)
        context.batchInvalidTree()
        
        return context
    }

    fun mount(root: Element?, component: Component, isMain: Boolean = true): BuildContext {
        if (root == null) {
            throw RuntimeException("Cannot mount to a null root element")
        }
        val ctx = BuildContext()
        if (isMain) {
            BuildContext.root = ctx
        } else {
             BuildContext.devTools = ctx
        }
        ctx.render(root, component)
        ctx.batchInvalidTree()
        return ctx
    }
}