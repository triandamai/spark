package example

import dom.BuildContext
import dom.Component
import dom.View
import example.component.CodePreview
import example.store.SourceCodes

class Profile : Component() {
    private val preview = CodePreview(SourceCodes.profile)

    override fun render(context: BuildContext): View {
        return content {
            div {
                className("min-h-screen bg-gradient-to-r from-cyan-500 to-blue-500 flex flex-col justify-center py-12 sm:px-6 lg:px-8")

                div {
                    className("sm:mx-auto sm:w-full sm:max-w-md")
                    
                    div {
                        className("bg-white py-8 px-4 shadow-xl rounded-lg sm:px-10")

                        div {
                            className("flex flex-col items-center pb-6 border-b border-gray-100")
                            div {
                                className("h-24 w-24 rounded-full bg-blue-50 flex items-center justify-center mb-4 ring-4 ring-blue-50")
                                rawHtml("""<svg class="h-12 w-12 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>""")
                            }
                            
                            h2 {
                                className("text-2xl font-bold text-gray-900")
                                val name = query<String>("name") ?: "Guest User"
                                text(name)
                            }
                            
                            p {
                                className("text-sm font-medium text-gray-500 mt-1")
                                val id = param<Any>("id")
                                text("ID: #$id")
                            }
                        }

                        div {
                            className("py-6 space-y-4")
                            div {
                                className("flex justify-between items-center")
                                span { className("text-sm text-gray-500"); text("Role") }
                                span { className("text-sm font-medium text-gray-900"); text("Developer") }
                            }
                            div {
                                className("flex justify-between items-center")
                                span { className("text-sm text-gray-500"); text("Status") }
                                span { className("inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800"); text("Active") }
                            }
                        }

                        div {
                            className("mt-2")
                            button {
                                className("w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200")
                                text("Back to Home")
                                on(dom.types.DomEvent.Click) {
                                    dom.Router.navigate("/")
                                }
                            }
                        }
                    }
                }
                preview()
            }
        }
    }
}