package de.groovybyte.chunky.maskplugin.tracer

import it.unimi.dsi.fastutil.ints.IntIntPair
import se.llbit.chunky.renderer.DefaultRenderManager
import se.llbit.chunky.renderer.TileBasedRenderer
import se.llbit.chunky.renderer.WorkerState
import se.llbit.chunky.renderer.scene.RayTracer
import se.llbit.math.Ray
import kotlin.math.floor

class ExtendedPreviewRenderer(
    val _id: String,
    val _name: String,
    protected var tracer: RayTracer
) : TileBasedRenderer() {
    var doInterlacing: Boolean = true
    var drawCrosshair: Boolean = false
    var doTargetHighlighting: Boolean = false

    override fun getId(): String = _id

    override fun getName(): String = _name

    override fun getDescription(): String = _name

    @Throws(InterruptedException::class)
    override fun render(manager: DefaultRenderManager) {
        val task = manager.renderTask
        task.update("Preview", 2, 0, "")

        val scene = manager.bufferedScene

        val width = scene.width
        val height = scene.height

        val fullWidth = scene.getFullWidth()
        val fullHeight = scene.getFullHeight()
        val cropX = scene.getCropX()
        val cropY = scene.getCropY()

        val cam = scene.camera()
        val halfWidth = width / (2.0 * height)
        val invHeight = 1.0 / height

        val target = Ray()
        val hit = scene.traceTarget(target)
        val tx = floor(target.o.x + target.d.x * Ray.OFFSET).toInt()
        val ty = floor(target.o.y + target.d.y * Ray.OFFSET).toInt()
        val tz = floor(target.o.z + target.d.z * Ray.OFFSET).toInt()

        val sampleBuffer = scene.sampleBuffer
        for (i in 0..1) {
            submitTiles(
                manager
            ) { state: WorkerState, pixel: IntIntPair ->
                val sx = pixel.firstInt()
                val sy = pixel.secondInt()
                val px = sx + cropX
                val py = sy + cropY

                val offset = 3 * (sy * width + sx)

                // Interlacing
                if(doInterlacing && (px + py) % 2 == i) return@submitTiles

                // Draw crosshairs
                if(drawCrosshair) {
                    if (px == fullWidth / 2 && py >= fullHeight / 2 - 5 && py <= fullHeight / 2 + 5
                        || py == fullHeight / 2 && px >= fullWidth / 2 - 5 && px <= fullWidth / 2 + 5
                    ) {
                        sampleBuffer[offset + 0] = 255.0
                        sampleBuffer[offset + 1] = 255.0
                        sampleBuffer[offset + 2] = 255.0
                        return@submitTiles
                    }
                }

                val x = -halfWidth + px * invHeight
                val y = -0.5 + py * invHeight
                state.ray = CanvasRay(x + .5, y + .5, px, py)
                cam.calcViewRay(
                    state.ray, state.random,
                    x, y
                )
                scene.rayTrace(tracer, state)

                // Target highlighting.
                if(doTargetHighlighting) {
                    val rx = Math.floor(state.ray.o.x + state.ray.d.x * Ray.OFFSET).toInt()
                    val ry = Math.floor(state.ray.o.y + state.ray.d.y * Ray.OFFSET).toInt()
                    val rz = Math.floor(state.ray.o.z + state.ray.d.z * Ray.OFFSET).toInt()
                    if (hit && tx == rx && ty == ry && tz == rz) {
                        state.ray.color.x = 1 - state.ray.color.x
                        state.ray.color.y = 1 - state.ray.color.y
                        state.ray.color.z = 1 - state.ray.color.z
                        state.ray.color.w = 1.0
                    }
                }

                sampleBuffer[offset + 0] = state.ray.color.x
                sampleBuffer[offset + 1] = state.ray.color.y
                sampleBuffer[offset + 2] = state.ray.color.z
                if (i == 0 && px < width - 1) {
                    sampleBuffer[offset + 3] = state.ray.color.x
                    sampleBuffer[offset + 4] = state.ray.color.y
                    sampleBuffer[offset + 5] = state.ray.color.z
                }
            }
            manager.pool.awaitEmpty()
            task.update(i + 1)
            if (postRender.asBoolean) break
        }
    }
}
