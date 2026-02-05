import dom.Application
import dom.Router
import example.Home
import example.Todo
import example.Profile
import example.Login
import example.MoveIt
import example.DragIt
import example.Bounced
import example.CanvasDemo
import example.UseDirective
import example.Transition
import kotlinx.browser.document


fun main() {
    val router = Router {
        route("/") { entry ->
            Home()
        }
        route("/todo") { entry ->
            Todo()
        }
        route("/profile/:id") { entry ->
            Profile()
        }
        route("/login") { entry ->
            Login()
        }
        route("/move") { entry ->
            MoveIt()
        }
        route("/drag") { entry ->
            DragIt()
        }
        route("/bounced") { entry ->
            Bounced()
        }
        route("/use-directive") { entry ->
            UseDirective()
        }
        route("/transitions") { entry ->
            Transition()
        }
        route("/canvas"){
            CanvasDemo()
        }
    }.beforeNavigate { from, to, next ->
       next()
    }


//    Application.mount(root, Home())
    Application.mount(document.getElementById("app"), router)
}
