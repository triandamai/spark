package internal

import internal.devtools.DevTools
import reactivity.State
import reactivity.Store
import kotlin.js.Date


data class NavEntry(
    val path: String = "",
    val parameters: Map<String, Any> = mapOf(),
    val query: Map<String, Any> = mapOf()
) {
    fun <T> getParam(key: String): T? = parameters[key] as T?
    fun <T> getQuery(key: String): T? = query[key] as T?
}

abstract class Component {
    val id: String = "Component_${uniqueId}"
    private var parent: String?=null
    open val enableDevTools: Boolean = true

    companion object {
        private var uniqueId = Date().getTime().toLong()
    }

    private var isMounted = false
    private val states = mutableListOf<State<*>>()
    private var stateIndex = 0
    private val childComponents = mutableListOf<Component>()
    private val keyedChildComponents = mutableMapOf<Any, Component>()
    private var childComponentIndex = 0
    private var navEntry = NavEntry()
    internal var _context: BuildContext? = null

    fun setParent(parent: Component) {
        this.parent = parent.id
    }
    fun getParent() = this.parent

    fun getComponentId() = this.id
    fun setNavEntry(entry: NavEntry) {
        navEntry = entry
    }

    fun <T> param(key: String) = navEntry.getParam<T>(key)
    fun <T> query(key: String) = navEntry.getQuery<T>(key)
    open fun beforeNavigate(from: Route, to: Route, next: () -> Boolean): Boolean {
        return next()
    }

    open fun onMounted() {
        if (!isMounted) {
            isMounted = true
            if (enableDevTools) {
                DevTools.registerComponent(id, this)
            }
        }
    }

    open fun onUnmounted() {
        if (isMounted) {
            isMounted = false
            clearStates()
            childComponents.forEach { it.onUnmounted() }
            keyedChildComponents.values.forEach { it.onUnmounted() }
            childComponents.clear()
            keyedChildComponents.clear()
            if (enableDevTools) {
                DevTools.unregisterComponent(id)
            }
        }
    }

    open fun onUpdated() {

    }

    fun resetStateIndex() {
        stateIndex = 0
        childComponentIndex = 0
    }

    fun isMounted() = isMounted

    open fun update(other: Component) {
        val thisKeys: Array<String> = js("Object").keys(this)

        for (key in thisKeys) {
            val otherValue = other.asDynamic()[key]

            if (otherValue !== undefined) {
                val typeofOther = js("typeof otherValue")
                if (typeofOther != "function" &&
                    !key.startsWith("isMounted") &&
                    !key.startsWith("states") &&
                    !key.startsWith("stateIndex") &&
                    !key.startsWith("childComponents") &&
                    !key.startsWith("keyedChildComponents") &&
                    !key.startsWith("childComponentIndex") &&
                    !key.startsWith("navEntry")
                ) {
                    try {
                        this.asDynamic()[key] = otherValue
                    } catch (e: Throwable) {
                        // Ignore read-only properties or other assignment errors
                    }
                }
            }
        }

        //mark lifecycle hook for update as the dependency is changed
        onUpdated()
    }

    fun <T : Component> child(vararg keys: Any?, factory: () -> T): T {
        if (keys.isNotEmpty() && keys[0] != null) {
            val key = keys[0]!!
            val existing = keyedChildComponents[key]
            if (existing != null) {
                @Suppress("UNCHECKED_CAST")
                val comp = existing as T
                val newComp = factory()
                comp.update(newComp)
                comp.resetStateIndex()
                return comp
            }
            val newComp = factory()
            keyedChildComponents[key] = newComp
            return newComp
        }

        if (childComponentIndex < childComponents.size) {
            val oldComp = childComponents[childComponentIndex]
            childComponentIndex++
            @Suppress("UNCHECKED_CAST")
            val comp = oldComp as T
            val newComp = factory()
            comp.update(newComp)
            comp.resetStateIndex()
            return comp
        }
        val newComp = factory()
        childComponents.add(newComp)
        childComponentIndex++
        return newComp as T
    }

    private fun transferStateFrom(other: Component) {
        for (i in 0 until minOf(states.size, other.states.size)) {
            @Suppress("UNCHECKED_CAST")
            val newState = states[i] as State<Any?>

            @Suppress("UNCHECKED_CAST")
            val oldState = other.states[i] as State<Any?>
            newState.setWithoutNotifying(oldState.value)
        }
        childComponents.clear()
        childComponents.addAll(other.childComponents)
        keyedChildComponents.clear()
        keyedChildComponents.putAll(other.keyedChildComponents)
        childComponentIndex = 0
    }

    fun <T> state(initialValue: T): State<T> {
        if (stateIndex < states.size) {
            @Suppress("UNCHECKED_CAST")
            val s = states[stateIndex] as State<T>
            stateIndex++
            return s
        }

        val s = State(initialValue)
        val idx = stateIndex
        s.subscribe {
            /**
             * Re-render from the root of this component's hierarchy
             *
             **/
            if (isMounted) {
                _context?.requestUpdate()
            }
            if (enableDevTools) {
                DevTools.logStateChange(id, idx,s.value)
            }
        }
        states.add(s)
        stateIndex++
        return s
    }


    fun <S, A : Any> useStore(store: Store<S, A>): Store<S, A> {
        store.subscribe {
            _context?.requestUpdate()
        }
        return store
    }

    fun clearStates() {
        states.clear()
        stateIndex = 0
    }

    abstract fun render(context: BuildContext): View

    fun content(block: ElementBuilder.() -> Unit): View {
        return View(block)
    }
}