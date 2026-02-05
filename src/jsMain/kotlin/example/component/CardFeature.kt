package example.component

import dom.BuildContext
import dom.Component
import dom.Router.Companion.navigate
import dom.View
import dom.types.DomEvent
import example.store.PageInfo

class CardFeature(private val page: PageInfo) : Component() {
    override fun render(context: BuildContext): View {
        return content {
            div {
                className("relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-500 rounded-2xl shadow-sm hover:shadow-md transition-all duration-200 border border-slate-100")
                
                div {
                    className("inline-flex p-3 rounded-lg bg-indigo-50 text-indigo-700 ring-4 ring-white")
                    rawHtml("""<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="${page.icon}"></path></svg>""")
                }

                div {
                    className("mt-8")
                    h3 {
                        className("text-lg font-semibold text-slate-900")
                        a {
                            className("focus:outline-none cursor-pointer")
                            on(DomEvent.Click) { e ->
                                e.preventDefault()
                                navigate(page.path)
                            }
                            span {
                                className("absolute inset-0")
                                attr("aria-hidden", "true")
                            }
                            text(page.name)
                        }
                    }
                    p {
                        className("mt-2 text-sm text-slate-500")
                        text(page.description)
                    }
                }

                span {
                    className("pointer-events-none absolute top-6 right-6 text-slate-300 group-hover:text-slate-400 transition-colors")
                    attr("aria-hidden", "true")
                    rawHtml("""<svg class="h-6 w-6" fill="currentColor" viewBox="0 0 24 24"><path d="M20 4h1a1 1 0 00-1-1v1zm-1 12a1 1 0 102 0h-2zM8 3a1 1 0 000 2V3zM3.293 19.293a1 1 0 101.414 1.414l-1.414-1.414zM19 4v12h2V4h-2zm1-1H8v2h12V3zm-.707.293l-16 16 1.414 1.414 16-16-1.414-1.414z" /></svg>""")
                }
            }
        }
    }
}
