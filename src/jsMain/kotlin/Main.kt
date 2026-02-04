import dom.Application
import dom.Router
import dom.Router.Companion.navigate
import example.App
import example.Home
import example.Profile
import example.Login
import example.Explore
import example.Explore2
import example.Bounced
import example.UseDirective
import kotlinx.browser.document
import kotlinx.browser.localStorage


fun main() {
    val router = Router {
        route("/") { entry ->
            App()
        }
        route("/home") { entry ->
            Home()
        }
        route("/profile/:id") { entry ->
            Profile()
        }
        route("/login") { entry ->
            Login()
        }
        route("/explore") { entry ->
            Explore()
        }
        route("/explore2") { entry ->
            Explore2()
        }
        route("/bounced") { entry ->
            Bounced()
        }
        route("/use-directive") { entry ->
            UseDirective()
        }
    }.beforeNavigate { from, to, next ->
       next()
    }


//    Application.mount(root, App())
    Application.mount(document.getElementById("app"), router)
}
