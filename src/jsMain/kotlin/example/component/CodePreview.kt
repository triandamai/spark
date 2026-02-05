package example.component

import dom.BuildContext
import dom.Component
import dom.View
import dom.types.DomEvent

class CodePreview(private val code: String) : Component() {
    private val isVisible = state(false)

    override fun render(context: BuildContext): View {
        return content {
            div {
                className("mt-12 w-full max-w-4xl mx-auto")
                
                button {
                    className("flex items-center space-x-2 text-slate-500 hover:text-indigo-600 font-medium transition-colors mb-4")
                    on(DomEvent.Click) {
                        console.log("clicked")
                        isVisible(!isVisible())
                        console.log("clicked"+isVisible())
                    }
                    
                    rawHtml(if (isVisible()) """
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path></svg>
                    """ else """
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
                    """)
                    text(if (isVisible()) "Hide Source Code" else "Show Source Code")
                }

                if (isVisible()) {
                    div {
                        className("bg-slate-900 rounded-xl overflow-hidden shadow-2xl transition-all duration-300")
                        div {
                            className("flex items-center justify-between px-4 py-2 bg-slate-800 border-b border-slate-700")
                            div {
                                className("flex space-x-1.5")
                                div { className("w-3 h-3 rounded-full bg-red-500") }
                                div { className("w-3 h-3 rounded-full bg-amber-500") }
                                div { className("w-3 h-3 rounded-full bg-emerald-500") }
                            }
                            span {
                                className("text-xs text-slate-400 font-mono")
                                text("Source Code")
                            }
                        }
                        pre {
                            className("p-6 overflow-x-auto text-sm text-slate-300 font-mono leading-relaxed")
                            code {
                                text(code)
                            }
                        }
                    }
                }
            }
        }
    }
}
