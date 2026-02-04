package example

import dom.BuildContext
import dom.Component
import dom.Route
import dom.Router.Companion.navigate
import dom.View
import dom.types.DomEvent
import example.component.Input
import example.component.ItemTodoView
import example.store.AuthAction
import example.store.ItemTodo
import example.store.TodoAction
import example.store.auth
import example.store.todo
import kotlinx.browser.localStorage
import reactivity.removeAt
import reactivity.set


class App : Component() {
    val store = useStore(auth)
    val todoStore = useStore(todo)
    val items = state(listOf<ItemTodo>())
    
    val form = Input({
        items.value = items.value + ItemTodo(it)
    })

    override fun beforeNavigate(from: Route, to: Route, next: () -> Boolean): Boolean {
        if (!to.path.startsWith("/login")) {
            val isLoggedIn = localStorage.getItem("isLoggedIn").toBoolean()
            if (!isLoggedIn) {
                return navigate("/login")
            }
            return next()
        }
        return next()
    }

    override fun render(context: BuildContext): View {
        return content {
            div {
                className("min-h-screen bg-gradient-to-br from-purple-600 to-indigo-700 flex items-center justify-center py-10 px-4 font-sans")

                div {
                    className("max-w-xl w-full bg-white rounded-xl shadow-2xl overflow-hidden")
                    div {
                        className("bg-white border-b border-gray-100 px-6 py-5 flex items-center justify-between")
                        div {
                            h1 {
                                className("text-2xl font-bold text-gray-800 tracking-tight")
                                text("My Tasks")
                            }
                            span {
                                className("text-sm text-gray-500 ml-2")
                                text("${todoStore.state.todos.size} requests")
                            }
                        }

                        div {
                            className("flex space-x-4 text-sm font-medium text-gray-500")
                            a {
                                className("hover:text-indigo-600 cursor-pointer transition-colors")
                                text("Explore")
                                on(DomEvent.Click) { e ->
                                    e.preventDefault()
                                    navigate("/explore")
                                }
                            }
                            a {
                                className("hover:text-indigo-600 cursor-pointer transition-colors")
                                text("Explore 2")
                                on(DomEvent.Click) { e ->
                                    e.preventDefault()
                                    navigate("/explore2")
                                }
                            }
                            a {
                                className("hover:text-indigo-600 cursor-pointer transition-colors")
                                text("Bounced")
                                on(DomEvent.Click) { e ->
                                    e.preventDefault()
                                    navigate("/bounced")
                                }
                            }
                            a {
                                className("hover:text-indigo-600 cursor-pointer transition-colors")
                                text("Directives")
                                on(DomEvent.Click) { e ->
                                    e.preventDefault()
                                    navigate("/use-directive")
                                }
                            }
                            a {
                                className("hover:text-indigo-600 cursor-pointer transition-colors")
                                text("Profile")
                                on(DomEvent.Click) { e ->
                                    e.preventDefault()
                                    navigate("/profile/1?name=Juan")
                                }
                            }
                            a {
                                className("hover:text-indigo-600 cursor-pointer transition-colors")
                                text("Login")
                                on(DomEvent.Click) { e ->
                                    e.preventDefault()
                                    store.dispatch(AuthAction.Logout)
                                }
                            }
                        }
                    }

                    div {
                        className("p-6 bg-gray-50/50 min-h-[400px]")
                        form()
                        div {
                            className("mt-6")
                            if (items.value.isEmpty()) {
                                div {
                                    className("text-center py-10 text-gray-400 border-2 border-dashed border-gray-200 rounded-lg")
                                    text("No tasks yet. Add one above!")
                                }
                            }

                            items.eachIndexed { index, item ->
                                component {
                                    ItemTodoView(
                                        item.name,
                                        isDone = item.done,
                                        onDone = { done ->
                                            val modify = item.copy(done = done)
                                            items.set(index, modify)
                                        },
                                        onRemove = {
                                            items.removeAt(index)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    div {
                        className("bg-gray-50 px-6 py-3 border-t border-gray-100 flex justify-between items-center")
                        button {
                            className("text-xs text-gray-400 hover:text-gray-600 underline")
                            text("Debug Tree")
                            on(DomEvent.Click) {
                                console.log(BuildContext.root)
                            }
                        }
                    }
                }
            }
        }
    }
}
