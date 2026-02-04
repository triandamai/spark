package example

import dom.BuildContext
import dom.Component
import dom.Directive
import dom.View
import dom.types.DomEvent
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

class TooltipDirective(private val text: String) : Directive {
    override fun update(element: Element) {
        element.setAttribute("title", text)
        (element as? HTMLElement)?.style?.cursor = "help"
    }
}

class UseDirective : Component() {
    val count = state(0)
    val focused = state(false)

    override fun render(context: BuildContext): View {
        return content {
            div {
                className("min-h-screen bg-slate-50 p-8 font-sans")
                
                div {
                    className("max-w-2xl mx-auto bg-white rounded-xl shadow-lg p-8")
                    
                    h1 {
                        className("text-3xl font-bold text-gray-800 mb-4")
                        text("Directive: 'use'")
                    }
                    
                    p {
                        className("text-gray-600 mb-8")
                        text("Directives allow you to access the underlying DOM element directly. This example shows auto-focusing, custom color manipulation, and class-based directives.")
                    }

                    div {
                        className("space-y-6")
                        
                        div {
                            label {
                                className("block text-sm font-medium text-gray-700 mb-1")
                                text("Auto-focusing Input (using function directive)")
                            }
                            input {
                                className("w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none")
                                placeholder("I will be focused on mount...")
                                use { el ->
                                    if (!focused.value) {
                                        (el as HTMLElement).focus()
                                        focused.value = true
                                        console.log("Input focused via directive")
                                    }
                                }
                            }
                        }

                        div {
                            label {
                                className("block text-sm font-medium text-gray-700 mb-1")
                                text("Element with Tooltip (using class-based directive)")
                            }
                            div {
                                className("inline-block px-4 py-2 bg-amber-100 text-amber-800 rounded-md border border-amber-200")
                                text("Hover over me for a tooltip")
                                use(TooltipDirective("This tooltip is set via a class-based directive!"))
                            }
                        }

                        div {
                            label {
                                className("block text-sm font-medium text-gray-700 mb-2")
                                text("Custom DOM Manipulation (Directives run on every patch)")
                            }
                            div {
                                className("h-32 w-full rounded-lg flex items-center justify-center text-white font-bold text-2xl transition-all duration-500")
                                text("Count: ${count.value}")
                                
                                use { el ->
                                    val element = el as HTMLElement
                                    val hue = (count.value * 20) % 360
                                    element.style.backgroundColor = "hsl($hue, 70%, 50%)"
                                    element.style.transform = "scale(${1.0 + (count.value % 5) * 0.05})"
                                }
                            }
                            
                            div {
                                className("flex space-x-4 mt-4")
                                button {
                                    className("bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition-colors")
                                    text("Increment State")
                                    on(DomEvent.Click) {
                                        count.value++
                                    }
                                }
                                button {
                                    className("bg-gray-200 text-gray-700 px-4 py-2 rounded hover:bg-gray-300 transition-colors")
                                    text("Reset")
                                    on(DomEvent.Click) {
                                        count.value = 0
                                    }
                                }
                            }
                        }
                    }

                    div {
                        className("mt-8 pt-6 border-t border-gray-100")
                        button {
                            className("text-indigo-600 hover:text-indigo-800 font-medium transition-colors")
                            text("Back to Dashboard")
                            on(DomEvent.Click) {
                                dom.Router.navigate("/")
                            }
                        }
                    }
                }
            }
        }
    }
}
