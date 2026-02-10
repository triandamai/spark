package internal.devtools

import internal.BuildContext
import internal.Component
import internal.View
import internal.builders.BaseElementBuilder
import internal.builders.DivBuilder
import internal.devtools.component.ContentDevtools
import internal.devtools.component.DevtoolFeature
import internal.devtools.component.SideBar
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CustomEvent
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.js.Date

data class LogEvent(
    val timestamp: Long,
    val type: String,
    val message: String,
    val details: String = "",
    val componentId: String? = null
)

data class StateChangeInfo(
    val stateName: String,
    val idx: Int,
    val value: String,
    val timestamp: Long,
    val componentId: String
)

data class ComponentInfo(
    val id: String,
    val name: String,
    val isMounted: Boolean
)


sealed class DevToolEvent(
    val timestamp: Long,
    val event: String,
) {
    data class Component(
        val id: String,
        val mounted: Boolean,
        val details: String,
    ) : DevToolEvent(
        timestamp = Date().getTime().toLong(),
        event = "Component",
    )

    data class StateMutation(
        val id: String,
        val name: String,
        val details: String,
    ) : DevToolEvent(
        timestamp = Date().getTime().toLong(),
        event = "State",
    )


    data class LogEvent(
        val id: String,
        val message: String,
        val details: String,
    ) : DevToolEvent(
        timestamp = Date().getTime().toLong(),
        event = "Log",
    )
}

enum class Edge { LEFT, RIGHT, TOP, BOTTOM }

data class ViewportSize(
    val width: Int,
    val height: Int
)

fun getAvailableViewport(): ViewportSize {
    // Try VisualViewport via dynamic (runtime-safe)
    val vv = window.asDynamic().visualViewport

    if (vv != null) {
        return ViewportSize(
            width = (vv.width as Double).toInt(),
            height = (vv.height as Double).toInt()
        )
    }

    // documentElement client size (most reliable fallback)
    val doc = document.documentElement
    if (doc != null) {
        return ViewportSize(
            width = doc.clientWidth,
            height = doc.clientHeight
        )
    }

    // Last resort
    return ViewportSize(
        width = window.innerWidth,
        height = window.innerHeight
    )
}

fun getElementSize(id: String): Pair<Double, Double> {
    val container = document.getElementById(id) as HTMLElement?
    return if (container != null) {
        val elementWidth = container.clientWidth.toDouble()
        val elementHeight = container.clientHeight.toDouble()
        Pair(elementWidth, elementHeight)
    } else Pair(0.0, 0.0)
}

class DevToolsUI : Component() {

    // Lifecycle events (mount/unmount)
    private val lifecycleEvents by useState<List<LogEvent>>(emptyList())

    // State changes: Map of "componentId:stateName" -> StateChangeInfo
    private val stateChanges by useState<Map<String, StateChangeInfo>>(emptyMap())

    // Map of ID -> ComponentInfo
    private val componentMap by useState<Map<String, ComponentInfo>>(emptyMap())

    private val selectedFeature by useState<DevtoolFeature?>(null)
    private val selectedComponentId by useState<String?>(null)
    private val isMinimized = state(true)

    // Drag state
    private val positionX by useState(0.0)
    private val positionY by useState(0.0)

    private val positionToggleX by useState((window.innerWidth / 2).toDouble())
    private val positionToggleY by useState(window.innerHeight.toDouble() - 40)
    private var isDragging = false
    private var isDragToggle = false
    private var dragStartX = 0.0
    private var dragStartY = 0.0
    private var refId: Int? = null

    // Prevent infinite recursion 
    override val enableDevTools: Boolean = false

    override fun onMounted() {
        super.onMounted()

        window.addEventListener("devtools-component-mount", { event ->
            val customEvent = event as CustomEvent
            val detail = customEvent.detail.unsafeCast<dynamic>()
            val id = detail.id as? String ?: return@addEventListener

            val name = detail.name as? String?
            if (name != null) {
                updateComponentStatus(id, name, true)
                addLifecycleEvent("Component Mounted", name, "ID: $id", id)
            }
        })

        window.addEventListener("devtools-component-unmount", { event ->
            val customEvent = event as CustomEvent
            val detail = customEvent.detail.unsafeCast<dynamic>()
            val id = detail.id as? String ?: return@addEventListener

            val currentInfo = componentMap.value[id]
            val name = currentInfo?.name
            if (name != null) {
                updateComponentStatus(id, name, false)
                addLifecycleEvent("Component Unmounted", name, "", id)
            }
        })

        window.addEventListener("devtools-state-change", { event ->
            val customEvent = event as CustomEvent
            val detail = customEvent.detail.unsafeCast<dynamic>()
            val componentId = detail.id as? String ?: return@addEventListener
            val idx = detail.idx as? Int ?: return@addEventListener
            val stateName = detail.name as? String ?: return@addEventListener
            val compName = detail.component as? String ?: return@addEventListener
            val value = detail.value as? String ?: ""

            updateStateChange(componentId, idx, stateName, compName, value)
        })

        // Global mouse event handlers for dragging

        window.addEventListener("resize", { e ->
            val viewPort = getAvailableViewport()
            val size = getElementSize("toggle")
            val edge = findEdge(
                viewPort = viewPort,
                x = positionToggleX(),
                y = positionToggleY(),
            )

            val padding = 5.0
            when (edge) {
                Edge.RIGHT -> positionToggleX(viewPort.width - (size.first + padding))
                Edge.BOTTOM -> positionToggleY(viewPort.height - (size.second + padding))
                else -> Unit
            }
        })

        window.addEventListener("mousemove", { e ->
            refId = window.requestAnimationFrame {
                handleMove(e)
            }
        })

        window.addEventListener("mouseup", {
            isDragging = false
            isDragToggle = false
        })


    }

    fun handleToggle(e: MouseEvent, x: Double, y: Double) {
        val (elementWidth, elementHeight) = getElementSize("toggle")
        val viewPort = getAvailableViewport()
        val w = viewPort.width
        val h = viewPort.height
        val closestEdge = findEdge(viewPort, x, y)

        when (closestEdge) {
            Edge.LEFT -> {
                if (y > 0 && y <= (h - elementHeight)) {
                    positionToggleY(y)
                }
                positionToggleX(0.0)
            }

            Edge.RIGHT -> {
                if (y > 0 && y <= (h - elementHeight)) {
                    positionToggleY(y)
                }
                positionToggleX(w.toDouble() - elementWidth)
            }

            Edge.TOP -> {
                positionToggleY(0.0)
                if (x > 0 && x < (w - elementWidth)) {
                    positionToggleX(x)
                }
            }

            Edge.BOTTOM -> {
                positionToggleY(h.toDouble() - elementHeight)
                if (x > 0 && x < (w - elementWidth)) {
                    positionToggleX(x)
                }
            }
        }

    }

    fun handleContainer(e: MouseEvent, x: Double, y: Double) {
        val (elementWidth,elementHeight) = getElementSize("container")
        val viewPort = getAvailableViewport()
            val screenWidth = viewPort.width
            val screenHeight = viewPort.height
            val x = e.clientX - dragStartX
            if (x > 0 && x <= (screenWidth - elementWidth)) {
                positionX(x)
            }
            val y = e.clientY - dragStartY
            if (y > 0 && y <= (screenHeight - elementHeight)) {
                positionY(y)
            }
    }

    fun handleMove(e: Event) {
        if (isDragging) {
            val mouseEvent = e.unsafeCast<MouseEvent>()
            if (isDragToggle) {
                val x = mouseEvent.clientX.toDouble()
                val y = mouseEvent.clientY.toDouble()
                handleToggle(mouseEvent, x, y)
            } else {
                val x = mouseEvent.clientX - dragStartX
                val y = mouseEvent.clientY - dragStartY
                handleContainer(mouseEvent, x, y)
            }
        }
    }


    private fun findEdge(viewPort: ViewportSize = getAvailableViewport(), x: Double, y: Double): Edge {
        val w = viewPort.width
        val h = viewPort.height


        val distances = mapOf(
            Edge.LEFT to x,
            Edge.RIGHT to (w - x),
            Edge.TOP to y,
            Edge.BOTTOM to (h - y)
        )

        return distances.minBy { it.value }.key
    }

    private fun showContainer(show: Boolean) {
        isMinimized(show)
        val (width, height) = getElementSize("container")
        val (toggleWidth, toggleHeight) = getElementSize("toggle")

        val viewPort = getAvailableViewport()
        val edge = findEdge(viewPort = viewPort, positionToggleX(), positionToggleY())

        val middleX = viewPort.width / 2
        val middleY = viewPort.height / 2

        val middleYElement = height / 2
        val middleXElement = width / 2

        val padding = 5.0
        val (x, y) = when (edge) {
            Edge.LEFT -> {
                val availableX = (0.0 + toggleWidth + padding)
                val y = if (positionToggleY() <= middleY) middleY - middleYElement
                else middleY - middleYElement
                (availableX to y)
            }

            Edge.RIGHT -> {
                val availableX = (viewPort.width - width) - toggleWidth
                val y = if (positionToggleY() <= middleY) middleY - middleYElement
                else middleY - middleYElement
                (availableX to y)
            }

            Edge.TOP -> {
                val availableY = (0.0 + toggleHeight + padding)
                val x = if (positionToggleX() <= middleX) middleX - middleXElement
                else middleX - middleXElement
                (x to availableY)
            }

            Edge.BOTTOM -> {
                val availableY = (viewPort.height - height) - toggleHeight
                val x = if (positionToggleX() <= middleX) middleX - middleXElement
                else middleX - middleXElement
                (x to availableY)
            }
        }

        positionX(x)
        positionY(y)
    }

    private fun updateComponentStatus(id: String, name: String, isMounted: Boolean) {
        val current = componentMap.value.toMutableMap()
        current[id] = ComponentInfo(id, name, isMounted)
        componentMap(current)
    }

    private fun addLifecycleEvent(type: String, message: String, details: String, componentId: String?) {
        val newEvent = LogEvent(Date.now().toLong(), type, message, details, componentId)
        val current = lifecycleEvents.value.toMutableList()
        current.add(0, newEvent)
        if (current.size > 50) current.removeLast()
        lifecycleEvents(current)
    }

    private fun updateStateChange(
        componentId: String,
        idx: Int,
        stateName: String,
        component: String,
        value: String
    ) {
        val key = "$componentId:$stateName:$idx"
        val current = stateChanges.value.toMutableMap()
        current[key] = StateChangeInfo(stateName, idx, value, Date.now().toLong(), componentId)
        stateChanges.value = current
    }


    override fun render(context: BuildContext): View = content {
        if (!DevTools.isEnabled) return@content

        div {
            className("fixed z-50 font-sans text-xs")
            attr("style", "left: ${positionX()}px; top: ${positionY()}px;")


            button {
                className("fixed bg-gray-900 text-green-400 p-3 rounded-full shadow-lg hover:bg-gray-800 transition-all border border-green-500/30")
                text("🛠️")
                attr("id", "toggle")
                attr("style", "left: ${positionToggleX()}px; top: ${positionToggleY()}px;")
                on("click") {
                    showContainer(!isMinimized.value)
                    isDragToggle = false
                }
                on("mousedown") { e ->
                    val mouseEvent = e.unsafeCast<MouseEvent>()
                    isDragging = true
                    isDragToggle = true
                    dragStartX = mouseEvent.clientX - positionX.value
                    dragStartY = mouseEvent.clientY - positionY.value
                    e.preventDefault()
                }
            }

            div {

                attr("id", "container")
                if (!isMinimized()) {
                    className("block bg-white text-gray-900 rounded-lg shadow-2xl w-[800px] h-[500px] flex flex-col overflow-hidden border border-gray-200")
                } else {
                    className("hidden bg-white text-gray-900 rounded-lg shadow-2xl w-[800px] h-[500px] flex flex-col overflow-hidden border border-gray-200")
                }
                // Header
                div {
                    className("flex justify-between items-center p-3 border-b border-gray-200 bg-gray-100 cursor-move select-none")
                    on("mousedown") { e ->
                        val mouseEvent = e.unsafeCast<MouseEvent>()
                        isDragging = true
                        isDragToggle = false
                        dragStartX = mouseEvent.clientX - positionX.value
                        dragStartY = mouseEvent.clientY - positionY.value
                        e.preventDefault()
                    }
                    div {
                        className("flex items-center gap-2")
                        span { text("🛠️") }
                        h3 {
                            className("font-bold text-gray-700")
                            text("Sparklin DevTools")
                        }
                    }
                    button {
                        className("text-gray-500 hover:text-gray-800 px-2 font-bold")
                        text("—")
                        on("click") {
                            showContainer(!isMinimized.value)
                        }
                    }
                }

                // Main Layout
                div {
                    className("flex flex-1 overflow-hidden")

                    // Sidebar
                    child(factory = {
                        SideBar(
                            features = listOf(
                                DevtoolFeature(
                                    name = "Component Tracing",
                                    id = "trace",
                                    description = "Tracing",
                                )
                            ),
                            selected = selectedFeature(),
                            onSelected = {
                                selectedFeature(it)
                                selectedComponentId.value = null // Reset selection when switching feature
                            }
                        )
                    }).invoke()


                    child {
                        ContentDevtools(
                            selectedFeature = selectedFeature(),
                            area = {
                                if (selectedFeature()?.id == "trace") {
                                    renderComponentsTracing()
                                } else {
                                    div {
                                        className("p-4 text-gray-400")
                                        text("Select a feature")
                                    }
                                }
                            }
                        )
                    }.invoke()
                }
            }
        }
    }


    private fun DivBuilder.renderComponentsTracing() {
        val selectedId = selectedComponentId()

        if (selectedId == null) {
            // LIST VIEW
            div {
                className("p-3 border-b border-gray-100 bg-white")
                h2 { className("text-lg font-semibold text-gray-800"); text("Components") }
            }
            div {
                className("flex-1 overflow-y-auto p-2")
                if (componentMap().isEmpty()) {
                    div { className("text-gray-400 italic p-4 text-center"); text("No components detected.") }
                } else {
                    componentMap().values.sortedBy { it.name }.each { info ->
                        element {
                            renderComponentItem(info)
                        }
                    }
                }
            }
        } else {
            // DETAIL VIEW
            val info = componentMap.value[selectedId]
            div {
                className("flex flex-col h-full")
                // Detail Header
                div {
                    className("flex items-center gap-3 p-3 border-b border-gray-100 bg-gray-50")
                    button {
                        className("text-gray-500 hover:text-gray-800 px-2 py-1 rounded hover:bg-gray-200")
                        text("← Back")
                        on("click") {
                            selectedComponentId.value = null
                        }
                    }
                    div {
                        h2 { className("text-lg font-semibold text-gray-800"); text(info?.name ?: "Unknown") }
                        span { className("text-gray-400 text-[10px] font-mono"); text(selectedId) }
                    }
                    div {
                        className("ml-auto")
                        if (info?.isMounted == true) {
                            span {
                                className("px-2 py-0.5 rounded-full bg-green-100 text-green-700 text-[10px] font-medium"); text(
                                "Active"
                            )
                            }
                        } else {
                            span {
                                className("px-2 py-0.5 rounded-full bg-gray-100 text-gray-500 text-[10px] font-medium"); text(
                                "Inactive"
                            )
                            }
                        }
                    }
                }

                // Component Details
                div {
                    className("flex-1 overflow-y-auto bg-white")

                    // State Changes Section
                    div {
                        className("border-b border-gray-100")
                        div {
                            className("p-3 bg-gray-50 border-b border-gray-100")
                            h3 { className("font-semibold text-gray-700 text-sm"); text("State") }
                        }

                        val componentStates = stateChanges.value.values.filter { it.componentId == selectedId }
                            .sortedByDescending { it.timestamp }

                        if (componentStates.isEmpty()) {
                            div { className("text-gray-400 italic p-4 text-center text-xs"); text("No state changes recorded.") }
                        } else {
                            componentStates.each { stateInfo ->
                                element {
                                    div {
                                        className("p-3 border-b border-gray-50 hover:bg-gray-50 transition-colors")
                                        div {
                                            className("flex justify-between items-baseline mb-1")
                                            span {
                                                className("font-medium text-blue-600 text-xs")
                                                text(stateInfo.stateName)
                                            }
                                            span { className("text-gray-400 text-[10px]"); text(formatTime(stateInfo.timestamp)) }
                                        }
                                        div {
                                            className("text-gray-700 break-words font-mono bg-gray-50 p-2 rounded mt-1 text-xs")
                                            text(stateInfo.value)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Lifecycle Events Section
                    div {
                        div {
                            className("p-3 bg-gray-50 border-b border-gray-100")
                            h3 { className("font-semibold text-gray-700 text-sm"); text("Lifecycle Events") }
                        }

                        val componentEvents = lifecycleEvents.value.filter { it.componentId == selectedId }

                        if (componentEvents.isEmpty()) {
                            div { className("text-gray-400 italic p-4 text-center text-xs"); text("No lifecycle events recorded.") }
                        } else {
                            componentEvents.each { event ->
                                element {
                                    div {
                                        className("p-3 border-b border-gray-50 hover:bg-gray-50 transition-colors")
                                        div {
                                            className("flex justify-between items-baseline mb-1")
                                            span {
                                                className("font-medium ${getEventTypeTextClass(event.type)} text-xs")
                                                text(event.type)
                                            }
                                            span { className("text-gray-400 text-[10px]"); text(formatTime(event.timestamp)) }
                                        }
                                        if (event.details.isNotEmpty()) {
                                            div {
                                                className("text-gray-600 break-words font-mono bg-gray-50 p-2 rounded mt-1 text-xs")
                                                text(event.details)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun BaseElementBuilder<*>.renderComponentItem(info: ComponentInfo) {
        div {
            className("flex items-center justify-between p-3 mb-2 rounded border border-gray-100 hover:border-blue-300 hover:shadow-sm cursor-pointer transition-all bg-white group")
            on("click") {
                selectedComponentId.value = info.id
            }

            div {
                className("flex flex-col")
                span { className("font-medium text-gray-700 group-hover:text-blue-600"); text(info.name) }
                span { className("text-gray-400 text-[10px] font-mono"); text(info.id) }
            }

            div {
                if (info.isMounted) {
                    span {
                        className("px-2 py-1 rounded-full bg-green-50 text-green-600 text-[10px] font-medium border border-green-100"); text(
                        "Active"
                    )
                    }
                } else {
                    span {
                        className("px-2 py-1 rounded-full bg-gray-50 text-gray-400 text-[10px] font-medium border border-gray-100"); text(
                        "Inactive"
                    )
                    }
                }
            }
        }
    }

    private fun getEventTypeTextClass(type: String): String {
        return when (type) {
            "Component Mounted" -> "text-green-600"
            "Component Unmounted" -> "text-red-500"
            "State Change" -> "text-blue-600"
            else -> "text-gray-600"
        }
    }

    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        return "${date.getHours().toString().padStart(2, '0')}:${
            date.getMinutes().toString().padStart(2, '0')
        }:${date.getSeconds().toString().padStart(2, '0')}"
    }
}
