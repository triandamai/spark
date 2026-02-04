package dom

import org.w3c.dom.Element

object Application {
    fun mount(root: Element?, router: Router): BuildContext {
        if (root == null) {
            throw RuntimeException("Cannot mount to a null root element")
        }
        val context = BuildContext()
        BuildContext.root = context
        router.mount(el = root, context = context)
        context.batchInvalidTree()
        return context
    }

    fun mount(root: Element?, component: Component): BuildContext {
        if (root == null) {
            throw RuntimeException("Cannot mount to a null root element")
        }
        val ctx = BuildContext()
        BuildContext.root = ctx
        ctx.render(root, component)
        ctx.batchInvalidTree()
        return ctx
    }
}