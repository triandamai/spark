package internal

import internal.builders.BaseElementBuilder.EachScope
import internal.directive.Directive
import internal.directive.DirectiveAction
import internal.transition.Transition
import kotlinx.browser.document

interface VNode {
    fun render(): org.w3c.dom.Node
    fun patch(old: VNode, node: org.w3c.dom.Node): org.w3c.dom.Node
}


class VText(val text: String) : VNode {
    override fun render(): org.w3c.dom.Node = document.createTextNode(text)
    override fun patch(old: VNode, node: org.w3c.dom.Node): org.w3c.dom.Node {
        if (old !is VText) {
            val newNode = render()
            node.parentNode?.replaceChild(newNode, node)
            return newNode
        }
        if (text != old.text) {
            node.textContent = text
        }
        return node
    }
}

class VRawHtml(val html: String) : VNode {
    override fun render(): org.w3c.dom.Node {
        val span = document.createElement("span")
        span.innerHTML = html
        return span
    }

    override fun patch(old: VNode, node: org.w3c.dom.Node): org.w3c.dom.Node {
        if (old !is VRawHtml) {
            val newNode = render()
            node.parentNode?.replaceChild(newNode, node)
            return newNode
        }
        if (html != old.html) {
            (node as org.w3c.dom.Element).innerHTML = html
        }
        return node
    }
}

class VElement(val tagName: String) : VNode {
    val attributes = mutableMapOf<String, String>()
    val listeners = mutableMapOf<String, (org.w3c.dom.events.Event) -> Unit>()
    val directives = mutableListOf<Any>()
    val children = mutableListOf<VNode>()
    var transitionIn: Transition? = null
    var transitionOut: Transition? = null

    override fun render(): org.w3c.dom.Node {
        val element = document.createElement(tagName) as org.w3c.dom.Element
        attributes.forEach { (name, value) ->
            if (name == "className") {
                element.asDynamic().className = value
            } else if (name == "value" && element is org.w3c.dom.HTMLInputElement) {
                element.value = value
            } else {
                element.setAttribute(name, value)
            }
        }
        listeners.forEach { (event, listener) ->
            element.addEventListener(event, listener)
        }
        children.forEach { child ->
            element.appendChild(child.render())
        }
        directives.forEach { directive ->
            when (directive) {
                is Directive -> directive.update(element)
                is DirectiveAction -> directive.update(element)
                is Function1<*, *> -> (directive as (org.w3c.dom.Element) -> Unit)(element)
            }
        }
        transitionIn?.start(element) {}
        return element
    }

    override fun patch(old: VNode, node: org.w3c.dom.Node): org.w3c.dom.Node {
        if (old !is VElement || old.tagName != tagName) {
            val newNode = render()
            node.parentNode?.replaceChild(newNode, node)
            return newNode
        }

        val element = node as org.w3c.dom.Element

        // Update attributes
        val allAttrNames = attributes.keys + old.attributes.keys
        allAttrNames.forEach { name ->
            val oldValue = old.attributes[name]
            val newValue = attributes[name]

            // For 'value' on input elements, we ALWAYS want to check if the DOM is in sync
            // because state might have changed to something that matches the Virtual DOM
            // but the DOM was updated by user input.
            if (name == "value" && element is org.w3c.dom.HTMLInputElement) {
                if (newValue != null && element.value != newValue) {
                    element.value = newValue
                }
            } else if (oldValue != newValue) {
                if (newValue == null) {
                    element.removeAttribute(name)
                } else {
                    if (name == "className") {
                        element.asDynamic().className = newValue
                    } else {
                        element.setAttribute(name, newValue)
                    }
                }
            }
        }

        // Update listeners - simplified: remove all old and add all new
        // A better way would be to track them, but for now this works.
        // Actually, removing and adding listeners is fine.
        old.listeners.forEach { (event, listener) ->
            element.removeEventListener(event, listener)
        }
        listeners.forEach { (event, listener) ->
            element.addEventListener(event, listener)
        }

        // Update directives
        // Directives are called on every patch to allow them to respond to updates if they want
        // though usually they might just set up once.
        // Svelte's 'use' usually has an 'update' and 'destroy' method.
        // For simplicity, we just call them again.
        directives.forEach { directive ->
            when (directive) {
                is Directive -> directive.update(element)
                is DirectiveAction -> directive.update(element)
                is Function1<*, *> -> (directive as (org.w3c.dom.Element) -> Unit)(element)
            }
        }

        // Handle directive destruction for directives in old but not in new
        // For simplicity, we are not doing full lifecycle management of directives here yet
        // but it could be added by comparing old.directives and new.directives.

        // Update children
        val domChildren = element.childNodes
        val currentNodes = mutableListOf<org.w3c.dom.Node>()
        for (i in 0 until domChildren.length) {
            currentNodes.add(domChildren.item(i)!!)
        }

        val max = maxOf(children.size, old.children.size)
        for (i in 0 until max) {
            if (i >= old.children.size) {
                element.appendChild(children[i].render())
            } else if (i >= children.size) {
                val oldVNode = old.children[i]
                val oldDomNode = currentNodes[i]
                if (oldVNode is VElement && oldVNode.transitionOut != null) {
                    oldVNode.transitionOut?.start(oldDomNode as org.w3c.dom.Element) {
                        if (oldDomNode.parentNode == element) {
                            element.removeChild(oldDomNode)
                        }
                    }
                } else {
                    element.removeChild(oldDomNode)
                }
            } else {
                children[i].patch(old.children[i], currentNodes[i])
            }
        }

        return element
    }
}

class View(val block: ElementBuilder.() -> Unit)


@DslMarker
annotation class ViewDsl


@ViewDsl
class ElementBuilder(val parent: VElement?) {
    val nodes = mutableListOf<VNode>()

    /**
     * Creates a div element.
     *
     * Example:
     * ```kotlin
     * div {
     *     className("container")
     *     text("Hello World")
     * }
     * ```
     */
    fun div(block: internal.builders.DivBuilder.() -> Unit) {
        val el = VElement("div")
        val builder = internal.builders.DivBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates a button element.
     *
     * Example:
     * ```kotlin
     * button {
     *     text("Click Me")
     *     on(DomEvent.Click) { println("Clicked!") }
     * }
     * ```
     */
    fun button(block: internal.builders.ButtonBuilder.() -> Unit) {
        val el = VElement("button")
        val builder = internal.builders.ButtonBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates a paragraph element.
     *
     * Example:
     * ```kotlin
     * p {
     *     text("This is a paragraph.")
     * }
     * ```
     */
    fun p(block: internal.builders.ParagraphBuilder.() -> Unit) {
        val el = VElement("p")
        val builder = internal.builders.ParagraphBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an input element.
     *
     * Example:
     * ```kotlin
     * input {
     *     type(InputType.Text)
     *     placeholder("Enter name")
     *     bind(usernameState)
     * }
     * ```
     */
    fun input(block: internal.builders.InputBuilder.() -> Unit) {
        val el = VElement("input")
        val builder = internal.builders.InputBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an anchor element.
     *
     * Example:
     * ```kotlin
     * a(href = "https://example.com") {
     *     text("Visit Example")
     * }
     * ```
     */
    fun a(href: String, block: internal.builders.AnchorBuilder.() -> Unit) {
        val el = VElement("a")
        el.attributes["href"] = href
        val builder = internal.builders.AnchorBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates a form element.
     *
     * Example:
     * ```kotlin
     * form {
     *     input { ... }
     *     button { text("Submit") }
     * }
     * ```
     */
    fun form(block: internal.builders.FormBuilder.() -> Unit) {
        val el = VElement("form")
        val builder = internal.builders.FormBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates a textarea element.
     *
     * Example:
     * ```kotlin
     * textarea {
     *     placeholder("Enter description")
     *     bind(descriptionState)
     * }
     * ```
     */
    fun textarea(block: internal.builders.TextAreaBuilder.() -> Unit) {
        val el = VElement("textarea")
        val builder = internal.builders.TextAreaBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates a select element.
     *
     * Example:
     * ```kotlin
     * select {
     *     bind(selectedOption)
     *     option { text("Option 1") }
     *     option { text("Option 2") }
     * }
     * ```
     */
    fun select(block: internal.builders.SelectBuilder.() -> Unit) {
        val el = VElement("select")
        val builder = internal.builders.SelectBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates a label element.
     *
     * Example:
     * ```kotlin
     * label {
     *     text("Username")
     * }
     * ```
     */
    fun label(block: internal.builders.LabelBuilder.() -> Unit) {
        val el = VElement("label")
        val builder = internal.builders.LabelBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates a span element.
     *
     * Example:
     * ```kotlin
     * span {
     *     className("badge")
     *     text("New")
     * }
     * ```
     */
    fun span(block: internal.builders.SpanBuilder.() -> Unit) {
        val el = VElement("span")
        val builder = internal.builders.SpanBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an h1 element.
     *
     * Example:
     * ```kotlin
     * h1 { text("Main Title") }
     * ```
     */
    fun h1(block: internal.builders.H1Builder.() -> Unit) {
        val el = VElement("h1")
        val builder = internal.builders.H1Builder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an h2 element.
     *
     * Example:
     * ```kotlin
     * h2 { text("Subtitle") }
     * ```
     */
    fun h2(block: internal.builders.H2Builder.() -> Unit) {
        val el = VElement("h2")
        val builder = internal.builders.H2Builder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an h3 element.
     *
     * Example:
     * ```kotlin
     * h3 { text("Section Header") }
     * ```
     */
    fun h3(block: internal.builders.H3Builder.() -> Unit) {
        val el = VElement("h3")
        val builder = internal.builders.H3Builder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an h4 element.
     *
     * Example:
     * ```kotlin
     * h4 { text("Level 4 Heading") }
     * ```
     */
    fun h4(block: internal.builders.H4Builder.() -> Unit) {
        val el = VElement("h4")
        val builder = internal.builders.H4Builder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an h5 element.
     *
     * Example:
     * ```kotlin
     * h5 { text("Level 5 Heading") }
     * ```
     */
    fun h5(block: internal.builders.H5Builder.() -> Unit) {
        val el = VElement("h5")
        val builder = internal.builders.H5Builder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an h6 element.
     *
     * Example:
     * ```kotlin
     * h6 { text("Level 6 Heading") }
     * ```
     */
    fun h6(block: internal.builders.H6Builder.() -> Unit) {
        val el = VElement("h6")
        val builder = internal.builders.H6Builder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an unordered list element.
     *
     * Example:
     * ```kotlin
     * ul {
     *     li { text("Item 1") }
     *     li { text("Item 2") }
     * }
     * ```
     */
    fun ul(block: internal.builders.UlBuilder.() -> Unit) {
        val el = VElement("ul")
        val builder = internal.builders.UlBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an ordered list element.
     *
     * Example:
     * ```kotlin
     * ol {
     *     li { text("First") }
     *     li { text("Second") }
     * }
     * ```
     */
    fun ol(block: internal.builders.OlBuilder.() -> Unit) {
        val el = VElement("ol")
        val builder = internal.builders.OlBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Creates an img element.
     *
     * Example:
     * ```kotlin
     * img {
     *     src("logo.png")
     *     alt("Company Logo")
     * }
     * ```
     */
    fun img(block: internal.builders.ImgBuilder.() -> Unit) {
        val el = VElement("img")
        val builder = internal.builders.ImgBuilder(el, this)
        builder.block()
        builder.finalize()
    }

    /**
     * Adds a text node.
     *
     * Example:
     * ```kotlin
     * text("Hello, Kotlin!")
     * ```
     */
    fun text(value: String) {
        addNode(VText(value))
    }

    /**
     * Adds a VNode to the current builder.
     */
    internal fun addNode(node: VNode) {
        if (parent != null) {
            parent.children.add(node)
        } else {
            nodes.add(node)
        }
    }


    /**
     * Invokes a component and adds it to the current builder.
     *
     * Example:
     * ```kotlin
     * MyComponent().invoke()
     * // or simply:
     * MyComponent()
     * ```
     */
    operator fun Component.invoke() {
        val context = BuildContext.current ?: BuildContext()
        context.addComponent(this)
        // this.resetStateIndex() // REMOVED: Managed by Component.child or already reset by root render
        val view = this.render(context)
        ComponentContext.withComponent(this) {
            view.block(this@ElementBuilder)
        }
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
    fun <ITEM> reactivity.State<List<ITEM>>.each(block: internal.builders.BaseElementBuilder.EachScope<ITEM, ElementBuilder>.(ITEM) -> Unit) {
        this.value.forEachIndexed { index, item ->
            val scope = internal.builders.BaseElementBuilder.EachScope<ITEM, ElementBuilder>(index, this@ElementBuilder)
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
    fun <ITEM> reactivity.State<List<ITEM>>.eachIndexed(block: internal.builders.BaseElementBuilder.EachScope<ITEM, ElementBuilder>.(Int, ITEM) -> Unit) {
        this.value.forEachIndexed { index, item ->
            val scope = internal.builders.BaseElementBuilder.EachScope<ITEM, ElementBuilder>(index, this@ElementBuilder)
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
    fun <ITEM> reactivity.State<MutableList<ITEM>>.each(block: internal.builders.BaseElementBuilder.EachScope<ITEM, ElementBuilder>.(ITEM) -> Unit) {
        this.value.forEachIndexed { index, item ->
            val scope = internal.builders.BaseElementBuilder.EachScope<ITEM, ElementBuilder>(index, this@ElementBuilder)
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
    fun <ITEM> reactivity.State<MutableList<ITEM>>.eachIndexed(block: internal.builders.BaseElementBuilder.EachScope<ITEM, ElementBuilder>.(Int, ITEM) -> Unit) {
        this.value.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, ElementBuilder>(index, this@ElementBuilder)
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
    fun <K, V> reactivity.State<Map<K, V>>.each(block: EachScope<Map<K, V>, ElementBuilder>.(Map.Entry<K, V>) -> Unit) {
        this.value.forEach { item ->
            val scope = EachScope<Map<K, V>, ElementBuilder>(item.key, this@ElementBuilder)
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
    fun <ITEM> List<ITEM>.each(block: EachScope<ITEM, ElementBuilder>.(ITEM) -> Unit) {
        this.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, ElementBuilder>(index, this@ElementBuilder)
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
    fun <ITEM> List<ITEM>.eachIndexed(block: EachScope<ITEM, ElementBuilder>.(Int, ITEM) -> Unit) {
        this.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, ElementBuilder>(index, this@ElementBuilder)
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
    fun <ITEM> MutableList<ITEM>.each(block: EachScope<ITEM, ElementBuilder>.(ITEM) -> Unit) {
        this.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, ElementBuilder>(index, this@ElementBuilder)
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
    fun <ITEM> MutableList<ITEM>.eachIndexed(block: EachScope<ITEM, ElementBuilder>.(Int, ITEM) -> Unit) {
        this.forEachIndexed { index, item ->
            val scope = EachScope<ITEM, ElementBuilder>(index, this@ElementBuilder)
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
    fun <K, V> Map<K, V>.each(block: EachScope<Map<K, V>, ElementBuilder>.(Map.Entry<K, V>) -> Unit) {
        this.forEach { item ->
            val scope = EachScope<Map<K, V>, ElementBuilder>(item.key, this@ElementBuilder)
            scope.block(item)
        }
    }
    /////
}