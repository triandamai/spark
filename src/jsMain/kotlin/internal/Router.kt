package internal

import internal.devtools.DevTools
import internal.devtools.DevToolsUI
import kotlinx.browser.window
import kotlinx.dom.clear
import org.w3c.dom.CustomEvent
import org.w3c.dom.Element

fun interface NavigationHook {
    fun beforeNavigate(from: Route, to: Route, next: () -> Boolean): Boolean
}

class RouterBuilder {
    private val routes: HashMap<String, Route> = HashMap()
    fun route(path: String, block: (NavEntry) -> Component) {
        val route = Route(path, block)
        this.routes[path] = route
    }

    fun getRoutes(): HashMap<String, Route> {
        return routes
    }
}

data class Route(
    val path: String,
    val component: (NavEntry) -> Component,
    val name: String? = null
)

class Router(
    builder: RouterBuilder.() -> Unit
) {
    private var currentPath = (window.location.pathname + window.location.search)
    private var root: Element? = null
    private var _routes = HashMap<String, Route>()
    private var navigationHook: NavigationHook? = null

    init {
        global = this
        _routes = RouterBuilder().apply(builder).getRoutes()
    }

    fun onMount() {
        window.onpopstate = {
            currentPath = window.location.pathname + window.location.search
            if (this.root == null) {
                throw RuntimeException("Cannot navigate to a non-root node")
            }
            if (BuildContext.root == null) {
                throw RuntimeException("Cannot navigate to a non-root BuildContext")
            }
            mount(root!!, context = BuildContext.root!!)
        }
    }

    fun navigate(path: String): Boolean {
        if (this.root == null) {
            throw RuntimeException("Cannot navigate to a non-root node")
        }
        if (BuildContext.root == null) {
            throw RuntimeException("Cannot navigate to a non-root BuildContext")
        }

        val (fromRoute, _) = findRoute(currentPath) ?: (_routes[currentPath] to mapOf())
        val defaultFrom = fromRoute ?: Route(currentPath, {
            object : Component() {
                override fun render(context: BuildContext) = content { text("Unknown") }
            }
        })

        val match = findRoute(path)
        val toRoute = match?.first ?: _routes["*"] ?: Route(path, {
            object : Component() {
                override fun render(context: BuildContext) = content { text("404") }
            }
        })
        val entry = NavEntry(
            path = path,
            parameters = match?.second ?: mapOf(),
            query = parseQuery(path),
        )
        val nextComponent = toRoute.component(entry)
        nextComponent.setNavEntry(entry)
        val next = {
            if(DevTools.isEnabled){
                DevTools.navigate()
            }
            Component.reset()
            window.history.pushState(null, "", path)
            currentPath = path
            mount(root!!, context = BuildContext.root!!)
            true
        }
        return navigationHook?.beforeNavigate(defaultFrom, toRoute) {
            nextComponent.beforeNavigate(defaultFrom, toRoute, next)
        } ?: false
    }

    fun mount(el: Element, context: BuildContext) {
        root = el
        onMount()


        val match = findRoute(currentPath)
        val factory = match?.first ?: _routes["*"]

        val ctn: Component = if (factory != null) {
            val entry = NavEntry(
                path = currentPath,
                parameters = match?.second ?: mapOf(),
                query = parseQuery(currentPath),
            )
            val comp = factory.component(entry)
            comp.setNavEntry(entry)
            comp
        } else {
            object : Component() {
                override fun render(context: BuildContext): View {
                    return content {
                        div {
                            text("404 Not Found")
                        }
                    }
                }
            }
        }
        val next = {
            el.clear()
            context.clearLastVNodes(el)

            if (factory != null && match?.first != null) {
                    ctn.beforeNavigate(factory, match.first, {
                        context.render(el, ctn)
                        true
                    })

            }else{
                context.render(el, ctn)
            }
            true
        }
        if (factory != null && match?.first != null) {
            navigationHook?.beforeNavigate(factory, match.first, next)
        }
    }

    private fun findRoute(path: String): Pair<Route, Map<String, String>>? {
        val pathPart = path.split("?")[0]
        // Exact match
        if (_routes.containsKey(pathPart)) {
            return _routes[pathPart]!! to mapOf()
        }

        // Regex match
        for (route in _routes.values) {
            // Convert /user/:id to regex ^/user/([^/]+)$
            if (!route.path.contains(":")) continue

            val parts = route.path.split("/")
            val regexPattern = "^" + parts.joinToString("/") { part ->
                if (part.startsWith(":")) "([^/]+)" else part
            } + "$"

            val regex = Regex(regexPattern)
            val matchResult = regex.find(pathPart)

            if (matchResult != null) {
                val params = mutableMapOf<String, String>()
                var groupIndex = 1
                parts.forEach { part ->
                    if (part.startsWith(":")) {
                        params[part.substring(1)] = matchResult.groupValues[groupIndex]
                        groupIndex++
                    }
                }
                return route to params
            }
        }
        return null
    }

    private fun parseQuery(path: String): Map<String, String> {
        if (!path.contains("?")) return mapOf()
        val queryPart = path.split("?")[1]
        val params = mutableMapOf<String, String>()
        queryPart.split("&").forEach { pair ->
            val parts = pair.split("=")
            if (parts.size == 2) {
                params[parts[0]] = parts[1]
            }
        }
        return params
    }

    fun beforeNavigate(hook: NavigationHook): Router {
        navigationHook = hook
        return this
    }

    companion object {
        fun navigate(path: String): Boolean {
            return global?.navigate(path) == true
        }

        var global: Router? = null
    }
}


fun useRouter(): Router{
    return Router.global ?: throw RuntimeException("Router is not initialized")
}