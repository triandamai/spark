package example

import internal.BuildContext
import internal.Component
import internal.View
import internal.types.DomEvent
import example.component.CodePreview
import example.store.SourceCodes
import kotlinx.browser.window

class Bounced : Component() {
    private val x = state<Double>(window.innerWidth.toDouble())
    private val y = state(100.0)
    private val vx = state(3.0)
    private val vy = state(0.0)

    private val preview = CodePreview(SourceCodes.bounced)
    private val gravity = 0.5
    private val bounce = -0.8
    private val friction = 0.99

    private val boxSize = 64.0
    private val containerHeight = 400.0
    private val containerWidth = 600.0

    private var animationHandle: Int? = null

    override fun onMounted() {
        super.onMounted()
        startAnimation()
    }

    override fun onUnmounted() {
        super.onUnmounted()
        animationHandle?.let { window.cancelAnimationFrame(it) }
    }

    private fun startAnimation() {
        fun step(timestamp: Double) {
            // Update physics
            vy.value += gravity

            var nextX = x.value + vx.value
            var nextY = y.value + vy.value

            // Wall collisions (X)
            if (nextX <= 0) {
                nextX = 0.0
                vx.value *= bounce
            } else if (nextX + boxSize >= containerWidth) {
                nextX = containerWidth - boxSize
                vx.value *= bounce
            }

            // Floor/Ceiling collisions (Y)
            if (nextY <= 0) {
                nextY = 0.0
                vy.value *= bounce
            } else if (nextY + boxSize >= containerHeight) {
                nextY = containerHeight - boxSize
                vy.value *= bounce
                // Add friction when hitting floor
                vx.value *= friction
            }

            x.value = nextX
            y.value = nextY

            animationHandle = window.requestAnimationFrame { step(it) }
        }
        animationHandle = window.requestAnimationFrame { step(it) }
    }

    override fun render(context: BuildContext): View = content {
        div {
            className("min-h-screen bg-slate-50 p-8 font-sans")

            div {

                div {
                    className("flex justify-between items-center mb-6")
                    div {
                        h1 {
                            className("text-3xl font-extrabold text-slate-900")
                            text("Bouncing Basketball")
                        }
                        p {
                            className("text-slate-500")
                            text("Physics simulation using requestAnimationFrame and State.")
                        }
                    }
                    button {
                        className("bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg font-medium transition-colors")
                        text("Reset Ball")
                        on(DomEvent.Click) {
                            x.value = 268.0
                            y.value = 100.0
                            vx.value = 3.0
                            vy.value = 0.0
                        }
                    }
                }

                div {
                    className("relative bg-slate-100 rounded-xl border-2 border-slate-200 overflow-hidden")
                    style("height", "${containerHeight}px")
                    style("width", "${containerWidth}px")
                    className("mx-auto")

                    // The "Ball"
                    div {
                        className("absolute bg-orange-500 rounded-full shadow-lg flex items-center justify-center border-4 border-orange-600")
                        style("width", "${boxSize}px")
                        style("height", "${boxSize}px")
                        style("left", "30%")
                        style("top", "0px")
                        style("transform", "translate(${x.value}px, ${y.value}px)")

                        // Basketball lines (simple SVG)
                        rawHtml(
                            """
                                <svg viewBox="0 0 100 100" class="w-full h-full p-1 opacity-40">
                                    <circle cx="50" cy="50" r="48" fill="none" stroke="black" stroke-width="2"/>
                                    <path d="M50 2 L50 98" fill="none" stroke="black" stroke-width="2"/>
                                    <path d="M2 50 L98 50" fill="none" stroke="black" stroke-width="2"/>
                                    <path d="M15 15 Q50 50 85 15" fill="none" stroke="black" stroke-width="2"/>
                                    <path d="M15 85 Q50 50 85 85" fill="none" stroke="black" stroke-width="2"/>
                                </svg>
                            """
                        )
                    }
                }

                div {
                    className("mt-8 pt-6 border-t border-slate-100 flex justify-between")
                    button {
                        className("text-indigo-600 hover:text-indigo-800 font-medium transition-colors")
                        text("Back to Home")
                        on(DomEvent.Click) {
                            internal.Router.navigate("/")
                        }
                    }
                    div {
                        className("text-slate-400 text-sm")
                        text("X: ${x.value.toInt()} | Y: ${y.value.toInt()}")
                    }
                }
            }
            preview()
        }
    }
}
