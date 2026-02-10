package internal.builders

import internal.*
import internal.directive.Directive
import internal.transition.BaseTransition
import internal.transition.Transition
import internal.types.DomEvent
import reactivity.State

@ViewDsl
abstract class BaseElementBuilder<T : BaseElementBuilder<T>>(
    protected val element: VElement,
    private val parentBuilder: ElementBuilder?
) {
    fun className(name: String): T {
        element.attributes["className"] = name
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    fun classname(name: String): T = className(name) // Handle typo compatibility

    open fun height(value: String) {
        element.attributes["height"] = value
    }

    fun style(name: String, value: String): T {
        val currentStyle = element.attributes["style"] as? String ?: ""
        val newStyle = if (currentStyle.isEmpty()) "$name: $value;" else "$currentStyle $name: $value;"
        element.attributes["style"] = newStyle
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    /**
     * Register an event listener using type-safe DomEvent.
     * Example: on(DomEvent.Click) { event -> ... }
     */
    fun on(event: DomEvent, listener: (org.w3c.dom.events.Event) -> Unit): T {
        element.listeners[event.value] = listener
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    /**
     * Register an event listener using a string (for backward compatibility).
     * Example: on("click") { event -> ... }
     */
    fun on(event: String, listener: (org.w3c.dom.events.Event) -> Unit): T {
        element.listeners[event] = listener
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    fun text(value: String) {
        element.children.add(VText(value))
    }

    fun text(value: State<*>) {
        element.children.add(VText("${value.value}"))
    }

    fun attr(name: String, value: String): T {
        element.attributes[name] = value
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    fun rawHtml(html: String) {
        element.children.add(VRawHtml(html))
    }

    /**
     * Use a directive on this element.
     * A directive can be a function, a class implementing Directive, or a DirectiveAction.
     * It receives the raw DOM element when it's created or updated.
     * This is useful for integrating with third-party libraries or accessing DOM-only APIs.
     * 
     * Example with function:
     * ```kotlin
     * div {
     *     use { el ->
     *         el.asDynamic().focus()
     *     }
     * }
     * ```
     * 
     * Example with class:
     * ```kotlin
     * class MyDirective : Directive {
     *     override fun update(element: Element) { ... }
     * }
     * div {
     *     use(MyDirective())
     * }
     * ```
     */
    fun use(directive: Directive): T {
        element.directives.add(directive)
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    fun use(directive: (org.w3c.dom.Element) -> Unit): T {
        element.directives.add(directive)
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    /**
     * Set a transition for this element (both in and out).
     */
    fun transition(transition: Transition): T {
        if (transition is BaseTransition) {
            element.transitionIn = transition.asIn()
            element.transitionOut = transition.asOut()
        } else {
            element.transitionIn = transition
            element.transitionOut = transition
        }
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    /**
     * Set an in-transition for this element.
     */
    fun inTransition(transition: Transition): T {
        if (transition is BaseTransition) {
            element.transitionIn = transition.asIn()
        } else {
            element.transitionIn = transition
        }
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    /**
     * Set an out-transition for this element.
     */
    fun outTransition(transition: Transition): T {
        if (transition is BaseTransition) {
            element.transitionOut = transition.asOut()
        } else {
            element.transitionOut = transition
        }
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    // Support for nested components
    operator fun Component.invoke() {
        val context = BuildContext.current ?: BuildContext()
        context.addComponent(this)
        // this.resetStateIndex() // REMOVED: Managed by Component.child or already reset by root render
        val view = this.render(context)
        // Create a temporary ElementBuilder to collect nodes
        val tempBuilder = ElementBuilder(null)
        ComponentContext.withComponent(this) {
            view.block(tempBuilder)
        }
        element.children.addAll(tempBuilder.nodes)
    }


    /**
     * Iterates over a State<List<T>> and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * items.each { item ->
     *     element { div { text(item) } }
     *     component { MyComponent(item) }
     * }
     * ```
     */
    fun <ITEM> reactivity.State<List<ITEM>>.each(block: EachScope<ITEM, BaseElementBuilder<*>>.(ITEM) -> Unit) {
        this.value.forEachIndexed { index, item ->
            val scope =
                EachScope<ITEM, BaseElementBuilder<*>>(index, this@BaseElementBuilder)
            scope.block(item)
        }
    }

    /**
     * Iterates over a State<List<T>> with an index and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * items.eachIndexed { index, item ->
     *     element { div { text("$index: $item") } }
     * }
     * ```
     */
    fun <ITEM> reactivity.State<List<ITEM>>.eachIndexed(block: EachScope<ITEM, BaseElementBuilder<*>>.(Int, ITEM) -> Unit) {
        this.value.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, BaseElementBuilder<*>>(index, this@BaseElementBuilder)
            scope.block(index, item)
        }
    }

    /**
     * Iterates over a State<MutableList<T>> and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * items.each { item ->
     *     element { div { text(item) } }
     * }
     * ```
     */
    fun <ITEM> reactivity.State<MutableList<ITEM>>.each(block: internal.builders.BaseElementBuilder.EachScope<ITEM, BaseElementBuilder<*>>.(ITEM) -> Unit) {
        this.value.forEach { item ->
            val scope = EachScope<ITEM, BaseElementBuilder<*>>(item, this@BaseElementBuilder)
            scope.block(item)
        }
    }

    /**
     * Iterates over a State<MutableList<T>> with an index and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * items.eachIndexed { index, item ->
     *     element { div { text("$index: $item") } }
     * }
     * ```
     */
    fun <ITEM> reactivity.State<MutableList<ITEM>>.eachIndexed(block: internal.builders.BaseElementBuilder.EachScope<ITEM, BaseElementBuilder<*>>.(Int, ITEM) -> Unit) {
        this.value.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, BaseElementBuilder<*>>(index, this@BaseElementBuilder)
            scope.block(index, item)
        }
    }

    /**
     * Iterates over a State<Map<K, V>> and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * mapState.each { entry ->
     *     element {
     *         div { text("${entry.key}: ${entry.value}") }
     *     }
     * }
     * ```
     */
    fun <K, V> reactivity.State<Map<K, V>>.each(block: EachScope<Map<K, V>, BaseElementBuilder<*>>.(Map.Entry<K, V>) -> Unit) {
        this.value.forEach { item ->
            val scope =
                EachScope<Map<K, V>, BaseElementBuilder<*>>(item.key, this@BaseElementBuilder)
            scope.block(item)
        }
    }


    //////

    /**
     * Iterates over a List<T> and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * items.each { item ->
     *     element { div { text(item) } }
     *     component { MyComponent(item) }
     * }
     * ```
     */
    fun <ITEM> List<ITEM>.each(block: EachScope<ITEM, BaseElementBuilder<*>>.(ITEM) -> Unit) {
        this.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, BaseElementBuilder<*>>(index,this@BaseElementBuilder)
            scope.block(item)
        }
    }

    /**
     * Iterates over a List<T> with an index and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * items.eachIndexed { index, item ->
     *     element { div { text("$index: $item") } }
     * }
     * ```
     */
    fun <ITEM> List<ITEM>.eachIndexed(block: EachScope<ITEM, BaseElementBuilder<*>>.(Int, ITEM) -> Unit) {
        this.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, BaseElementBuilder<*>>(index,this@BaseElementBuilder)
            scope.block(index, item)
        }
    }

    /**
     * Iterates over a MutableList<T> and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * items.each { item ->
     *     element { div { text(item) } }
     * }
     * ```
     */
    fun <ITEM> MutableList<ITEM>.each(block: EachScope<ITEM, BaseElementBuilder<*>>.(ITEM) -> Unit) {
        this.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, BaseElementBuilder<*>>(index, this@BaseElementBuilder)
            scope.block(item)
        }
    }

    /**
     * Iterates over a MutableList<T> with an index and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * items.eachIndexed { index, item ->
     *     element { div { text("$index: $item") } }
     * }
     * ```
     */
    fun <ITEM> MutableList<ITEM>.eachIndexed(block: EachScope<ITEM, BaseElementBuilder<*>>.(Int, ITEM) -> Unit) {
        this.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, BaseElementBuilder<*>>(index, this@BaseElementBuilder)
            scope.block(index, item)
        }
    }

    /**
     * Iterates over a Map<K, V> and provides a DSL for rendering elements or components.
     *
     * Example:
     * ```kotlin
     * mapState.each { entry ->
     *     element {
     *         div { text("${entry.key}: ${entry.value}") }
     *     }
     * }
     * ```
     */
    fun <K, V> Map<K, V>.each(block: EachScope<Map<K, V>, BaseElementBuilder<*>>.(Map.Entry<K, V>) -> Unit) {
        this.forEach { item ->
            val scope = EachScope<Map<K, V>, BaseElementBuilder<*>>(item.key, this@BaseElementBuilder)
            scope.block(item)
        }
    }

    /////
    class EachScope<ITEM, B>(val idx: Any?, val builder: B) {
        fun element(block: B.() -> Unit) {
            builder.block()
        }

        fun component(key: () -> Any? = { idx }, factory: () -> Component) {
            val parent = ComponentContext.current
            val keyed = key()
            val comp = parent?.child(keyed) { factory() } ?: factory()
            if (builder is BaseElementBuilder<*>) {
                builder.apply {
                    if(parent != null) {
                        comp.setParent(parent)
                    }
                    comp.invoke()
                }
            } else if (builder is ElementBuilder) {
                builder.apply {
                    if(parent != null) {
                        comp.setParent(parent)
                    }
                    comp.invoke()
                }
            }
        }
    }


    internal fun finalize() {
        parentBuilder?.addNode(element)
    }

    fun div(block: DivBuilder.() -> Unit) {
        val el = VElement("div")
        val builder = DivBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun button(block: ButtonBuilder.() -> Unit) {
        val el = VElement("button")
        val builder = ButtonBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun p(block: ParagraphBuilder.() -> Unit) {
        val el = VElement("p")
        val builder = ParagraphBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun input(block: InputBuilder.() -> Unit) {
        val el = VElement("input")
        val builder = InputBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun a(href: String = "", block: AnchorBuilder.() -> Unit) {
        val el = VElement("a")
        el.attributes["href"] = href
        val builder = AnchorBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun form(block: FormBuilder.() -> Unit) {
        val el = VElement("form")
        val builder = FormBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun textarea(block: TextAreaBuilder.() -> Unit) {
        val el = VElement("textarea")
        val builder = TextAreaBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun select(block: SelectBuilder.() -> Unit) {
        val el = VElement("select")
        val builder = SelectBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun label(block: LabelBuilder.() -> Unit) {
        val el = VElement("label")
        val builder = LabelBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun span(block: SpanBuilder.() -> Unit) {
        val el = VElement("span")
        val builder = SpanBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun h1(block: H1Builder.() -> Unit) {
        val el = VElement("h1")
        val builder = H1Builder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun h2(block: H2Builder.() -> Unit) {
        val el = VElement("h2")
        val builder = H2Builder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun h3(block: H3Builder.() -> Unit) {
        val el = VElement("h3")
        val builder = H3Builder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun h4(block: H4Builder.() -> Unit) {
        val el = VElement("h4")
        val builder = H4Builder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun h5(block: H5Builder.() -> Unit) {
        val el = VElement("h5")
        val builder = H5Builder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun h6(block: H6Builder.() -> Unit) {
        val el = VElement("h6")
        val builder = H6Builder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun ul(block: UlBuilder.() -> Unit) {
        val el = VElement("ul")
        val builder = UlBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun ol(block: OlBuilder.() -> Unit) {
        val el = VElement("ol")
        val builder = OlBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun pre(block: DivBuilder.() -> Unit) {
        val el = VElement("pre")
        val b = DivBuilder(el, parentBuilder)
        b.block()
        element.children.add(el)
    }

    fun code(block: DivBuilder.() -> Unit) {
        val el = VElement("code")
        val b = DivBuilder(el, parentBuilder)
        b.block()
        element.children.add(el)
    }

    fun img(block: ImgBuilder.() -> Unit) {
        val el = VElement("img")
        val builder = ImgBuilder(el, null)
        builder.block()
        element.children.add(el)
    }

    fun canvas(block: CanvasBuilder.() -> Unit) {
        val el = VElement("canvas")
        val builder = CanvasBuilder(el, parentBuilder)
        builder.block()
        element.children.add(el)
    }
}
