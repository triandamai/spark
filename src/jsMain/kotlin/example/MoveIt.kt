package example

import dom.BuildContext
import dom.Component
import dom.View
import dom.types.DomEvent
import dom.types.InputType
import example.component.CodePreview
import example.store.SourceCodes

class MoveIt : Component() {
    val translateX = state(0)
    val translateY = state(0)

    private val preview = CodePreview(SourceCodes.moveIt)

    override fun render(context: BuildContext): View {
        return content {
            div {
                className("min-h-screen bg-gray-100 p-8 font-sans")
                
                div {
                    className("max-w-2xl mx-auto bg-white rounded-xl shadow-lg p-8")
                    
                    h1 {
                        className("text-3xl font-bold text-gray-800 mb-6")
                        text("Explore Translation")
                    }

                    div {
                        className("grid grid-cols-2 gap-8 mb-8")
                        
                        div {
                            label {
                                className("block text-sm font-medium text-gray-700 mb-2")
                                text("Translate X: ${translateX.value}px")
                            }
                            input {
                                className("w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer")
                                type(InputType.Range)
                                bind(translateX)
                                attr("min", "-100")
                                attr("max", "100")
                            }
                        }

                        div {
                            label {
                                className("block text-sm font-medium text-gray-700 mb-2")
                                text("Translate Y: ${translateY.value}px")
                            }
                            input {
                                className("w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer")
                                type(InputType.Range)
                                bind(translateY)
                                attr("min", "-100")
                                attr("max", "100")
                            }
                        }
                    }

                    div {
                        className("flex items-center justify-center h-64 bg-gray-50 border-2 border-dashed border-gray-200 rounded-lg overflow-hidden")
                        
                        div {
                            className("w-32 h-32 bg-indigo-500 rounded-lg shadow-lg flex items-center justify-center text-white font-bold transition-transform duration-75 ease-out")
                            style("transform", "translate(${translateX.value}px, ${translateY.value}px)")
                            text("Move Me!")
                        }
                    }

                    div {
                        className("mt-8 pt-6 border-t border-gray-100")
                        button {
                            className("text-indigo-600 hover:text-indigo-800 font-medium transition-colors")
                            text("Back to Home")
                            on(DomEvent.Click) {
                                dom.Router.navigate("/")
                            }
                        }
                    }
                }
                preview()
            }
        }
    }
}
