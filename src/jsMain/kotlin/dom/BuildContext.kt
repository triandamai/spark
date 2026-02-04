package dom

import org.w3c.dom.Element


class BuildContext {
    private var rootElement: Element? = null
    var rootComponent: Component? = null
        private set
    private var activeComponents = mutableSetOf<Component?>()
    private var renderedComponents = mutableSetOf<Component?>()
    private val lastVNodesMap = mutableMapOf<Element, List<VNode>>()

    private var batchCleaning = false

    fun batchInvalidTree() {
        activeComponents = activeComponents.filter { it != null && it != undefined }.toMutableSet()
        renderedComponents = renderedComponents.filter { it != null && it != undefined }.toMutableSet()
        batchCleaning = false
    }

    fun addComponent(component: Component) {
        renderedComponents.add(component)
        // Also add all current children of this component recursively
        // component.getAllActiveChildren().forEach { renderedComponents.add(it) }
        // Actually, we don't know children until they are rendered/invoked.
        // But since we are doing a full render from root, all active components WILL be added via addComponent.
    }

    fun clearLastVNodes(container: Element) {
        lastVNodesMap.remove(container)
    }


    fun requestUpdate() {
        rootComponent?.let {
            render(rootElement!!, it)
            if (!batchCleaning) {
                batchCleaning = true
                batchInvalidTree()
            }
        }
    }

    fun render(container: Element, component: Component) {
        rootElement = container
        rootComponent = component
        // Always ensure root is set to the most recent BuildContext that is rendering a root component
        root = this

        current = this
        renderedComponents.clear()
        addComponent(component)
        component.resetStateIndex()
        val view = component.render(this)
        val builder = ElementBuilder(null)
        ComponentContext.withComponent(component) {
            view.block(builder)
        }
        current = null


        val removed = activeComponents.filter { it !in renderedComponents }
        removed.forEach { 
            it?.onUnmounted()
        }

        val added = renderedComponents.filter { it !in activeComponents }
        added.forEach { 
            it?.onMounted() 
        }

        activeComponents.clear()
        activeComponents.addAll(renderedComponents.filterNotNull())

        // Reconciliation using patching
        val newVNodes = builder.nodes
        val lastVNodes = lastVNodesMap[container] ?: emptyList()
        val domChildren = container.childNodes

        val max = maxOf(newVNodes.size, lastVNodes.size)
        // Adjust domChildren for the fact that they might change during loop if we are not careful.
        // But since we are only appending/removing at the current index, it should be fine.
        // Actually, removing i-th child will shift subsequent children. 
        // So it's better to iterate backwards or be very careful.

        // Let's use a more stable approach for children update
        val currentNodes = mutableListOf<org.w3c.dom.Node>()
        for (i in 0 until domChildren.length) {
            currentNodes.add(domChildren.item(i)!!)
        }

        for (i in 0 until max) {
            if (i >= lastVNodes.size) {
                container.appendChild(newVNodes[i].render())
            } else if (i >= newVNodes.size) {
                container.removeChild(currentNodes[i])
            } else {
                newVNodes[i].patch(lastVNodes[i], currentNodes[i])
            }
        }
        lastVNodesMap[container] = newVNodes
    }

    companion object {
        var current: BuildContext? = null
        var root: BuildContext? = null
    }
}
