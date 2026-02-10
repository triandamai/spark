package example

import internal.BuildContext
import internal.Component
import internal.View
import internal.types.DomEvent
import example.component.CodePreview
import example.store.SourceCodes
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class CanvasDemo : Component() {
    private val preview = CodePreview(SourceCodes.canvas)
    private val rotation by useState(0.0)
    private var animationHandle: Int? = null

    override fun onMounted() {
        super.onMounted()
        startAnimation()
    }

    override fun onUnmounted() {
        super.onUnmounted()
        animationHandle?.let { kotlinx.browser.window.cancelAnimationFrame(it) }
    }

    private fun startAnimation() {
        fun step(timestamp: Double) {
            rotation.value += 0.02
            animationHandle = kotlinx.browser.window.requestAnimationFrame { step(it) }
        }
        animationHandle = kotlinx.browser.window.requestAnimationFrame { step(it) }
    }

    override fun render(context: BuildContext): View = content {
        div {
            className("min-h-screen bg-slate-50 p-8 font-sans")

            div {
                className("max-w-4xl mx-auto bg-white rounded-2xl shadow-xl p-8")

                div {
                    className("flex justify-between items-center mb-8")
                    div {
                        h1 {
                            className("text-3xl font-extrabold text-slate-900")
                            text("Canvas API")
                        }
                        p {
                            className("text-slate-500")
                            text("Directly interact with the HTML5 Canvas using the 'use' directive.")
                        }
                    }
                    button {
                        className("text-indigo-600 hover:text-indigo-800 font-medium transition-colors")
                        text("Back to Home")
                        on(DomEvent.Click) {
                            internal.Router.navigate("/")
                        }
                    }
                }

                div {
                    className("flex flex-col items-center")

                    canvas {
                        width("600")
                        height("400")
                        className("border border-slate-200 rounded-xl shadow-inner bg-slate-900")
                        use { el ->
                            val canvas = el as HTMLCanvasElement
                            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
                            val w = canvas.width.toDouble()
                            val h = canvas.height.toDouble()

                            // Clear
                            ctx.clearRect(0.0, 0.0, w, h)

                            // Draw Rotating Squares
                            val centerX = w / 2
                            val centerY = h / 2
                            val size = 100.0

                            ctx.save()
                            ctx.translate(centerX, centerY)
                            ctx.rotate(rotation.value)

                            // Indigo square
                            ctx.fillStyle = "rgba(99, 102, 241, 0.8)"
                            ctx.fillRect(-size / 2, -size / 2, size, size)

                            // Rose square
                            ctx.rotate(PI / 4)
                            ctx.fillStyle = "rgba(244, 63, 94, 0.8)"
                            ctx.fillRect(-size / 2, -size / 2, size, size)

                            ctx.restore()

                            // Draw some particles
                            for (i in 0..10) {
                                val angle = rotation.value + i * (PI * 2 / 10)
                                val x = centerX + cos(angle) * 150
                                val y = centerY + sin(angle) * 150

                                ctx.beginPath()
                                ctx.arc(x, y, 5.0, 0.0, PI * 2)
                                ctx.fillStyle = "white"
                                ctx.fill()
                            }
                        }
                    }

                    div {
                        className("mt-6 p-4 bg-indigo-50 rounded-lg border border-indigo-100 text-indigo-800 text-sm max-w-lg text-center")
                        text("The canvas is redrawn on every state change (rotation) via the 'use' directive's update hook.")
                    }
                }

                div {
                    className("mt-12 pt-8 border-t border-slate-100")
                    preview()
                }
            }
        }
    }
}

