package example

import dom.BuildContext
import dom.Component
import dom.View
import dom.types.DomEvent
import dom.types.InputType
import example.store.AuthAction
import example.store.auth
import kotlinx.browser.window


class Login : Component() {
    val username = state("")
    val password = state("")
    val selectedRole = state("")
    val options = state(
        mutableListOf(
            "Admin", "User"
        )
    )
    val store = useStore(auth)

    override fun render(context: BuildContext): View {
        return content {
            div {
                className("min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8")
                div {
                    className("max-w-md w-full space-y-8 bg-white p-10 rounded-xl shadow-2xl")
                    div {
                        h2 {
                            className("mt-6 text-center text-3xl font-extrabold text-gray-900")
                            text("Sign in to your account")
                        }
                    }
                    div {
                        className("mt-8 space-y-6")
                        div {
                            className("rounded-md shadow-sm -space-y-px")
                            div {
                                className("mb-4")
                                label {
                                    className("block text-sm font-medium text-gray-700 mb-1")
                                    text("Username")
                                }
                                input {
                                    className("appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm")
                                    type(InputType.Text)
                                    placeholder("Username")
                                    bind(username)
                                }
                            }
                            div {
                                className("mb-4")
                                label {
                                    className("block text-sm font-medium text-gray-700 mb-1")
                                    text("Password")
                                }
                                input {
                                    className("appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm")
                                    type(InputType.Password)
                                    placeholder("Password")
                                    bind(password)
                                    on(DomEvent.KeyDown) {
                                        if(it.asDynamic().code == "Enter"){
                                            store.dispatch(AuthAction.Login(username(), password()))
                                        }
                                    }
                                }
                            }
                            div {
                                className("mb-4")
                            }
                        }

                        div {
                            button {
                                className("group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-150 ease-in-out")
                                text("Sign in")
                                on(DomEvent.Click) {
                                    store.dispatch(AuthAction.Login(username(), password()))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
