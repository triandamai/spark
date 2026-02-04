package example

import dom.BuildContext
import dom.Component
import dom.View
import dom.types.DomEvent
import org.w3c.dom.events.MouseEvent

class Explore2 : Component() {
    val x = state(0)
    val y = state(0)
    val isDragging = state(false)
    
    private var startX = 0
    private var startY = 0
    private var initialX = 0
    private var initialY = 0

    override fun render(context: BuildContext): View {
        return content {
            div {
                className("min-h-screen bg-slate-100 p-8 font-sans")
                
                div {
                    className("max-w-2xl mx-auto bg-white rounded-xl shadow-lg p-8")
                    
                    h1 {
                        className("text-3xl font-bold text-gray-800 mb-2")
                        text("Explore 2: Self-Moving Div")
                    }
                    p {
                        className("text-gray-600 mb-6")
                        text("Click and drag the box below to move it around. It manages its own position state.")
                    }

                    div {
                        className("flex items-center justify-center h-96 bg-gray-50 border-2 border-dashed border-gray-200 rounded-lg overflow-hidden relative")
                        
                        div {
                            className("w-32 h-32 bg-rose-500 rounded-lg shadow-xl flex flex-col items-center justify-center text-white font-bold cursor-move select-none absolute")
                            style("transform", "translate(${x.value}px, ${y.value}px)")
                            style("transition", if (isDragging.value) "none" else "transform 0.1s ease-out")
                            
                            on(DomEvent.MouseDown) { e ->
                                val mouseEvent = e as MouseEvent
                                isDragging.value = true
                                startX = mouseEvent.clientX
                                startY = mouseEvent.clientY
                                initialX = x.value
                                initialY = y.value
                                
                                // To handle mouse move outside the div, we'd ideally attach to window,
                                // but for this DSL example we'll show it on the container or keep it simple.
                            }

                            text("Drag Me")
                            span {
                                className("text-xs mt-2 opacity-80")
                                text("X: ${x.value}, Y: ${y.value}")
                            }
                        }
                        
                        // Overlay to catch mouse move when dragging
                        if (isDragging.value) {
                            div {
                                className("fixed inset-0 z-50 cursor-move")
                                on(DomEvent.MouseMove) { e ->
                                    val mouseEvent = e as MouseEvent
                                    if (isDragging.value) {
                                        x.value = initialX + (mouseEvent.clientX - startX)
                                        y.value = initialY + (mouseEvent.clientY - startY)
                                    }
                                }
                                on(DomEvent.MouseUp) {
                                    isDragging.value = false
                                }
                            }
                        }
                    }

                    div {
                        className("mt-8 pt-6 border-t border-gray-100 flex justify-between")
                        button {
                            className("text-indigo-600 hover:text-indigo-800 font-medium transition-colors")
                            text("Back to Dashboard")
                            on(DomEvent.Click) {
                                dom.Router.navigate("/")
                            }
                        }
                        button {
                            className("text-gray-500 hover:text-gray-700 font-medium transition-colors")
                            text("Reset Position")
                            on(DomEvent.Click) {
                                x.value = 0
                                y.value = 0
                            }
                        }
                    }
                }
            }
        }
    }
}
