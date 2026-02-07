package example

import internal.BuildContext
import internal.Component
import internal.Route
import internal.Router.Companion.navigate
import internal.View
import internal.types.DomEvent
import example.component.CardFeature
import example.store.AuthAction
import example.store.PageInfo
import example.store.auth
import kotlinx.browser.localStorage

class Home : Component() {
    val store = useStore(auth)
    
    val pages = listOf(
        PageInfo(
            "Todo List",
            "/todo",
            "Manage your tasks with our reactive todo list.",
            "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
        ),
        PageInfo("Move It", "/move", "Interactive sliders to translate elements in 2D space.", "M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"),
        PageInfo("Drag It", "/drag", "Directly drag elements to update their state position.", "M7 11.5V14m0-2.5v-6a1.5 1.5 0 113 0V12m-3-1.5a3 3 0 00-6 0v6.75c0 .621.504 1.125 1.125 1.125h9.75a1.125 1.125 0 001.125-1.125V16.5a1.5 1.5 0 00-3 0V12"),
        PageInfo("Bounced", "/bounced", "High-frequency physics simulation of a bouncing ball.", "M13 10V3L4 14h7v7l9-11h-7z"),
        PageInfo("Directives", "/use-directive", "Direct DOM access using Svelte-like 'use' directives.", "M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"),
        PageInfo("Transitions", "/transitions", "Smooth entry and exit animations using DSL.", "M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z"),
        PageInfo("Canvas", "/canvas", "Smooth entry and exit animations using DSL.", "M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z"),
        PageInfo("Profile", "/profile/1?name=Junie", "View user profile and handle URL parameters/queries.", "M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z")
    )

    override fun beforeNavigate(from: Route, to: Route, next: () -> Boolean): Boolean {
//        if (!to.path.startsWith("/login")) {
//            val isLoggedIn = localStorage.getItem("isLoggedIn").toBoolean()
//            if (!isLoggedIn) {
//                return navigate("/login")
//            }
//            return next()
//        }
        return next()
    }

    override fun render(context: BuildContext): View = content {
        div {
            className("min-h-screen bg-slate-50 py-12 px-4 sm:px-6 lg:px-8 font-sans")

            div {
                    
                    div {
                        className("text-center mb-12")
                        h1 {
                            className("text-4xl font-extrabold text-slate-900 tracking-tight sm:text-5xl")
                            text("Spark")
                        }
                    
                        p {
                            className("mt-4 text-xl text-slate-600 max-w-2xl mx-auto")
                            text("Small, lightweight, but starts something big.")
                        }
                    }

                    div {
                        className("grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3")
                        
                        pages.each { page ->
                            component {
                                CardFeature(page)
                            }
                        }
                    }

                    div {
                        className("mt-12 pt-8 border-t border-slate-200 flex justify-between items-center")
                        div {
                            className("flex items-center space-x-4")
                            button {
                                className("text-sm font-medium text-slate-600 hover:text-indigo-600 transition-colors")
                                text("Sign Out")
                                on(DomEvent.Click) {
                                    store.dispatch(AuthAction.Logout)
                                }
                            }
                        }
                        div {
                            button {
                                className("text-xs text-slate-400 hover:text-slate-600 underline")
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
