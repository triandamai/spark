package example

import internal.*
import internal.transition.fade
import internal.transition.fly
import internal.types.DomEvent
import example.component.CodePreview
import example.store.SourceCodes

class Transition : Component() {
    val show by useState(true)
    val items by useState(mutableListOf("Item 1", "Item 2", "Item 3"))

    private val preview = CodePreview(SourceCodes.transition)

    override fun render(context: BuildContext): View = content {
        div {
            className("min-h-screen bg-slate-50 p-8 font-sans")

            div {
                className("max-w-3xl mx-auto bg-white rounded-2xl shadow-xl p-8")

                h1 {
                    className("text-3xl font-extrabold text-slate-900 mb-2")
                    text("Transitions & Animations")
                }
                p {
                    className("text-slate-500 mb-8")
                    text("Demonstrating Svelte-like 'transition', 'in', and 'out' directives using Kotlin DSL.")
                }

                div {
                    className("space-y-12")

                    // Basic Toggle with Fade
                    div {
                        h2 {
                            className("text-xl font-bold text-slate-800 mb-4")
                            text("Basic Toggle (Fade)")
                        }
                        button {
                            className("bg-indigo-600 text-white px-4 py-2 rounded-lg mb-4 hover:bg-indigo-700 transition-colors")
                            text(if (show.value) "Hide Box" else "Show Box")
                            on(DomEvent.Click) { show.value = !show.value }
                        }

                        div {
                            className("h-32")
                            if (show.value) {
                                div {
                                    className("w-48 h-24 bg-amber-400 rounded-lg shadow-lg flex items-center justify-center text-amber-900 font-bold")
                                    transition(fade(500))
                                    text("Fading Box")
                                }
                            }
                        }
                    }

                    // Different In/Out with Fly
                    div {
                        h2 {
                            className("text-xl font-bold text-slate-800 mb-4")
                            text("In/Out Transitions (Fly)")
                        }
                        div {
                            className("h-32")
                            if (show.value) {
                                div {
                                    className("w-48 h-24 bg-emerald-400 rounded-lg shadow-lg flex items-center justify-center text-emerald-900 font-bold")
                                    inTransition(fly(x = -100, duration = 400))
                                    outTransition(fly(x = 100, duration = 400))
                                    text("Flying Box")
                                }
                            }
                        }
                    }

                    // List Transitions
                    div {
                        h2 {
                            className("text-xl font-bold text-slate-800 mb-4")
                            text("List Transitions")
                        }
                        div {
                            className("flex space-x-2 mb-4")
                            button {
                                className("bg-blue-500 text-white px-3 py-1 rounded")
                                text("Add Item")
                                on(DomEvent.Click) {
                                    items.value = (items.value + "Item ${items.value.size + 1}").toMutableList()
                                }
                            }
                            button {
                                className("bg-red-500 text-white px-3 py-1 rounded")
                                text("Remove Last")
                                on(DomEvent.Click) {
                                    if (items.value.isNotEmpty()) {
                                        items.value = items.value.dropLast(1).toMutableList()
                                    }
                                }
                            }
                        }

                        div {
                            className("space-y-2")
                            items.each { item ->
                                element {
                                    div {
                                        className("p-3 bg-slate-100 rounded-md border border-slate-200 flex justify-between items-center")
                                        transition(fly(y = 20, duration = 300))
                                        text(item)
                                        button {
                                            className("text-red-500 hover:text-red-700")
                                            text("×")
                                            on(DomEvent.Click) {
                                                items.value = items.value.filter { it != item }.toMutableList()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                div {
                    className("mt-12 pt-6 border-t border-slate-100")
                    button {
                        className("text-indigo-600 hover:text-indigo-800 font-medium transition-colors")
                        text("Back to Home")
                        on(DomEvent.Click) {
                            internal.Router.navigate("/")
                        }
                    }
                }
            }
            preview()
        }
    }
}
