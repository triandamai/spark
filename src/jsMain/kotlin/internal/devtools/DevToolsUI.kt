package internal.devtools

import internal.BuildContext
import internal.Component
import internal.ElementBuilder
import internal.View
import internal.builders.BaseElementBuilder
import internal.builders.DivBuilder
import internal.types.DomEvent
import kotlinx.browser.window
import org.w3c.dom.CustomEvent
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
    ): DevToolEvent(
        timestamp = Date().getTime().toLong(),
        event = "State",
    )


    data class LogEvent(
        val id: String,
        val message: String,
        val details: String,
    ): DevToolEvent(
        timestamp = Date().getTime().toLong(),
        event = "Log",
    )
}

class DevToolsUI : Component() {

    // Lifecycle events (mount/unmount)
    private val lifecycleEvents = state<List<LogEvent>>(emptyList())

    // State changes: Map of "componentId:stateName" -> StateChangeInfo
    private val stateChanges = state<Map<String, StateChangeInfo>>(emptyMap())

    // Map of ID -> ComponentInfo
    private val componentMap = state<Map<String, ComponentInfo>>(emptyMap())

    private val selectedFeature = state("Components Tracing")
    private val selectedComponentId = state<String?>(null)
    private val isMinimized = state(true)

    // Drag state
    private val positionX = state(100.0)
    private val positionY = state(100.0)
    private var isDragging = false
    private var dragStartX = 0.0
    private var dragStartY = 0.0

    // Prevent infinite recursion 
    override val enableDevTools: Boolean = false

    override fun onMounted() {
        super.onMounted()

        window.addEventListener("devtools-component-mount", { event ->
            val customEvent = event as CustomEvent
            val detail = customEvent.detail.unsafeCast<dynamic>()
            val id = detail.id as? String ?: return@addEventListener
            if (id == this.id) return@addEventListener

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
            val value = detail.value as? String ?: ""

            updateStateChange(componentId, idx, stateName, value)
        })

        // Global mouse event handlers for dragging
        window.addEventListener("mousemove", { e ->
            if (isDragging) {
                val mouseEvent = e.unsafeCast<org.w3c.dom.events.MouseEvent>()
                val x = mouseEvent.clientX - dragStartX
                if (x > 0) {
                    if (x < window.innerWidth - 100) {
                        positionX(x)
                    }
                }
                val y = mouseEvent.clientY - dragStartY
                if (y > 0) {
                    if (y < window.innerHeight - 10) {
                        positionY(y)
                    }
                }


            }
        })

        window.addEventListener("mouseup", {
            isDragging = false
        })
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

    private fun updateStateChange(componentId: String, idx: Int, stateName: String, value: String) {
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

            if (isMinimized()) {
                button {
                    className("bg-gray-900 text-green-400 p-3 rounded-full shadow-lg hover:bg-gray-800 transition-all border border-green-500/30")
                    text("🛠️")
                    attr("style", "left: ${positionX()}px; top: ${positionY()}px;")
                    on("click") {
                        isMinimized.value = false
                    }
                    on("mousedown") { e ->
                        val mouseEvent = e.unsafeCast<org.w3c.dom.events.MouseEvent>()
                        isDragging = true
                        dragStartX = mouseEvent.clientX - positionX.value
                        dragStartY = mouseEvent.clientY - positionY.value
                        e.preventDefault()
                    }
                }
            } else {
                div {
                    className("bg-white text-gray-900 rounded-lg shadow-2xl w-[800px] h-[500px] flex flex-col overflow-hidden border border-gray-200")

                    // Header
                    div {
                        className("flex justify-between items-center p-3 border-b border-gray-200 bg-gray-100 cursor-move select-none")
                        on("mousedown") { e ->
                            val mouseEvent = e.unsafeCast<org.w3c.dom.events.MouseEvent>()
                            isDragging = true
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
                                isMinimized.value = true
                            }
                        }
                    }

                    // Main Layout
                    div {
                        className("flex flex-1 overflow-hidden")

                        // Sidebar
                        div {
                            className("w-64 bg-gray-50 border-r border-gray-200 flex flex-col")
                            div {
                                className("p-2 font-semibold text-gray-500 uppercase text-[10px] tracking-wider")
                                text("Features")
                            }
                            // Feature List
                            div {
                                className("px-2")
                                div {
                                    val isActive = selectedFeature.value == "Components Tracing"
                                    className("p-2 rounded cursor-pointer transition-colors ${if (isActive) "bg-blue-100 text-blue-700 font-medium" else "hover:bg-gray-100 text-gray-600"}")
                                    text("Components Tracing")
                                    on("click") {
                                        selectedFeature.value = "Components Tracing"
                                        selectedComponentId.value = null // Reset selection when switching feature
                                    }
                                }
                            }
                        }

                        // Content Area
                        div {
                            className("flex-1 flex flex-col overflow-hidden bg-white")

                            val activeFeature = selectedFeature.value
                            if (activeFeature == "Components Tracing") {
                                renderComponentsTracing()
                            } else {
                                div {
                                    className("p-4 text-gray-400")
                                    text("Select a feature")
                                }
                            }
                        }
                    }
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
                if (componentMap.value.isEmpty()) {
                    div { className("text-gray-400 italic p-4 text-center"); text("No components detected.") }
                } else {
                    componentMap.value.values.sortedBy { it.name }.each { info ->
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
                                                text(stateInfo.stateName.plus("-").plus(stateInfo.idx))
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
                span { className("text-gray-400 text-[10px] font-mono"); text(info.id.takeLast(8)) }
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
