package example.store

import dom.useRouter
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.set
import reactivity.createStore
data class AuthState(val isLoggedIn: Boolean = false)
sealed class AuthAction{
    data class Login(
        val email: String,
        val password: String
    ) : AuthAction()

    data object Logout : AuthAction()
}
val auth = createStore<AuthState, AuthAction>(AuthState()){
    on(AuthAction.Login::class){ action ->
        val router = useRouter()
        if(action.email == "admin" && action.password == "12345678"){
            localStorage.set("isLoggedIn", "true")
            router.navigate("/")
        }else{
            window.alert("Invalid credentials")
        }
    }
    on(AuthAction.Logout::class){
        val router = useRouter()
        localStorage.set("isLoggedIn", "false")
        router.navigate("/login")

    }
}