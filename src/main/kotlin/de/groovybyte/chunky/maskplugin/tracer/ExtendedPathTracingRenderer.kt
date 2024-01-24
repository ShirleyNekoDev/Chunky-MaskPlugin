package de.groovybyte.chunky.maskplugin.tracer

import it.unimi.dsi.fastutil.ints.IntIntPair
import se.llbit.chunky.renderer.DefaultRenderManager
import se.llbit.chunky.renderer.PathTracingRenderer
import se.llbit.chunky.renderer.WorkerState
import se.llbit.chunky.renderer.scene.RayTracer
import java.util.function.BiConsumer

class ExtendedPathTracingRenderer(
    id: String,
    name: String,
    description: String,
    rayTracer: RayTracer,
) : PathTracingRenderer(id, name, description, rayTracer) {

    var cropX: Int = 0
    var cropY: Int = 0
    var halfWidth: Double = 0.0
    var invHeight: Double = 0.0

    override fun render(manager: DefaultRenderManager) {
        val scene = manager.bufferedScene

        val width = scene.width
        val height = scene.height

        cropX = scene.getCropX()
        cropY = scene.getCropY()

        halfWidth = width / (2.0 * height)
        invHeight = 1.0 / height

        super.render(manager)
    }

    override fun submitTiles(
        manager: DefaultRenderManager,
        perPixel: BiConsumer<WorkerState, IntIntPair>
    ) {
        super.submitTiles(manager) { state, pixel ->
            val sx = pixel.firstInt()
            val sy = pixel.secondInt()
            val px = sx + cropX
            val py = sy + cropY
            val x = -halfWidth + px * invHeight
            val y = -0.5 + py * invHeight
            state.ray = CanvasRay(x + .5, y + .5, px, py)
            perPixel.accept(state, pixel)
        }
    }
}
