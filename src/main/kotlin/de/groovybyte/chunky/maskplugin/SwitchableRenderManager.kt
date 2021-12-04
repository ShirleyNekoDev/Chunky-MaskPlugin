package de.groovybyte.chunky.maskplugin

import de.groovybyte.chunky.maskplugin.utils.getSafeFieldGetterFor
import se.llbit.chunky.main.Chunky
import se.llbit.chunky.renderer.RenderContext
import se.llbit.chunky.renderer.RenderManager
import se.llbit.chunky.renderer.Repaintable
import se.llbit.chunky.renderer.scene.RayTracer
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.math.Vector4
import se.llbit.util.TaskTracker
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * @author Maximilian Stiede
 */
class SwitchableRenderManager(
    context: RenderContext,
    headless: Boolean
) : RenderManager(context, headless) {
    val enableMask: Boolean = false
    val maskRayTracer: MaskPathTracer = MaskPathTracer()

    override fun getRayTracer(): RayTracer =
        if (enableMask) maskRayTracer
        else super.getRayTracer()

//    override fun setOnRenderCompleted(listener: BiConsumer<Long, Int>) {
//        super.setOnRenderCompleted { time: Long, sps: Int ->
//            println("on frame completed")
//            println(maskRayTracer.foundMaterials.joinToString("\n"))
//            listener.accept(time, sps)
//        }
//    }

    private val stepSize = 512
    fun renderMask(scene: Scene, taskTracker: TaskTracker) {
        val executor = Chunky.getCommonThreads()

        taskTracker.task("Render mask").use { task ->
            val width = scene.width
            val height = scene.height

            val counter = AtomicInteger(0)
            var lastUpdate = 0L

            val sceneSize = width * height
            val total = ceil((width * height) * 1.0 / stepSize).toInt()
            task.update(sceneSize, 0)

            val tasks = ArrayList<CompletableFuture<Pair<Int, Array<Vector4>>>>(total)
            var pixelIndex = 0
            while (pixelIndex < sceneSize) {
                val range = pixelIndex until minOf(pixelIndex + stepSize, sceneSize)
                tasks.add(
                    CompletableFuture.supplyAsync(
                        {
                            val x = range.first % width
                            val y = range.first / width
                            val result = Array(range.count()) { i ->
                                maskRayTracer.rotatedGridSupersampling((x + i) % width, y, scene)
                            }
                            counter.incrementAndGet().also { done ->
                                System.currentTimeMillis().also { currentTime ->
                                    if (currentTime - lastUpdate > 200) {
                                        // update once every 200ms
                                        task.update(done * stepSize)
                                        lastUpdate = currentTime
                                    }
                                }
                            }
                            range.first to result
                        }, executor
                    )
                )
                pixelIndex += stepSize
            }
            CompletableFuture.allOf(*tasks.toTypedArray()).join()
            task.update(sceneSize)

            task.update("Finalize", 1, 0)
            tasks.forEach {
                val data = it.join()
                data.second.forEachIndexed { i, pixel ->
                    pixelIndex = data.first + i
                    val rgbIndex = pixelIndex * 3
                    scene.alphaChannel[pixelIndex] = (pixel.w * 255).roundToInt().toByte()
                    if (pixel.w <= 1.0) {
                        val x = (pixelIndex % width) / 8
                        val y = (pixelIndex / width) / 8
                        val transparencyGrid = if ((x + y) % 2 == 0) 0.4 else 0.2
                        pixel.x = pixel.x * pixel.w + transparencyGrid * (1 - pixel.w)
                        pixel.y = pixel.y * pixel.w + transparencyGrid * (1 - pixel.w)
                        pixel.z = pixel.z * pixel.w + transparencyGrid * (1 - pixel.w)
                    }
                    scene.sampleBuffer[rgbIndex + 0] = pixel.x
                    scene.sampleBuffer[rgbIndex + 1] = pixel.y
                    scene.sampleBuffer[rgbIndex + 2] = pixel.z
                }
            }

            for (y in 0 until height)
                for (x in 0 until width)
                    scene.finalizePixel(x, y)
            task.update(1)
//            scene.swapBuffers()
        }
        getCanvas().repaint()

//        scene.outputMode.write()
    }

    private val getCanvas = RenderManager::class.java
        .getSafeFieldGetterFor<RenderManager, Repaintable>("canvas")
    val canvas get() = getCanvas()
}
