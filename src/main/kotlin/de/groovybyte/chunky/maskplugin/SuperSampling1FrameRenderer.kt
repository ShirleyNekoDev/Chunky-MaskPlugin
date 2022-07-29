package de.groovybyte.chunky.maskplugin

import de.groovybyte.chunky.maskplugin.math.SuperSampling
import se.llbit.chunky.chunk.BlockPalette
import se.llbit.chunky.main.Chunky
import se.llbit.chunky.renderer.DefaultRenderManager
import se.llbit.chunky.renderer.Renderer
import se.llbit.chunky.renderer.ResetReason
import se.llbit.chunky.renderer.WorkerState
import se.llbit.chunky.renderer.scene.RayTracer
import se.llbit.math.Vector4
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BooleanSupplier
import kotlin.math.ceil

/**
 * @author Maximilian Stiede
 */
open class SuperSampling1FrameRenderer(
    private val _name: String,
    var superSampling: SuperSampling,
    private val rayTracer: RayTracer,
    private val updatePaletteCallback: (palette: BlockPalette) -> Unit = {},
) : Renderer {
    override fun getName(): String = _name

    override fun getDescription(): String = ""

    override fun getId(): String = _name

    lateinit var postFrameUpdateCallback: () -> Boolean
    override fun setPostRender(callback: BooleanSupplier) {
        postFrameUpdateCallback = callback::getAsBoolean
    }

    override fun sceneReset(manager: DefaultRenderManager, reason: ResetReason, resetCount: Int) {
        when (reason) {
            ResetReason.SCENE_LOADED -> updatePaletteCallback(manager.bufferedScene.palette)
            else -> {}
        }
    }

    private val stepSize = 512
    override fun render(manager: DefaultRenderManager) {
        val scene = manager.bufferedScene
        scene.spp = 0
        postFrameUpdateCallback()

        val width = scene.width
        val height = scene.height

//        val sppPerPass = manager.context.sppPerPass()

        val sampleBuffer = scene.sampleBuffer
        val alphaChannel = scene.alphaChannel

        val executor = Chunky.getCommonThreads()

        val counter = AtomicInteger(0)
        var lastUpdate = 0L

        val sceneSize = width * height
        val total = ceil((width * height) * 1.0 / stepSize).toInt()

        val tasks = ArrayList<CompletableFuture<Pair<Int, Array<Vector4>>>>(total)
        var pixelIndex = 0
        while (pixelIndex < sceneSize) {
            val range = pixelIndex until minOf(pixelIndex + stepSize, sceneSize)
            tasks.add(
                CompletableFuture.supplyAsync(
                    {
                        val state = WorkerState()
                        state.random = ThreadLocalRandom.current()
                        val x = range.first % width
                        val y = range.first / width
                        val result = Array(range.count()) { i ->
                            superSampling.sample(
                                (x + i) % width,
                                y,
                                scene,
                                state,
                                rayTracer::trace
                            )
                            state.ray.color
                        }
//                        Thread.sleep(Random.nextLong(100, 200))
                        counter.incrementAndGet().also { done ->
                            System.currentTimeMillis().also { currentTime ->
                                if (currentTime - lastUpdate > 200) {
                                    // update once every 200ms
                                    // TODO: update task
                                    lastUpdate = currentTime
//                                    postFrameUpdateCallback()
//                                    if(postFrameUpdateCallback()) {
//                                         TODO: abort - render was canceled
//                                    }
                                }
                            }
                        }
                        range.first to result
                    }, executor
                )
            )
            pixelIndex += stepSize
        }

//        CompletableFuture.allOf(*tasks.toTypedArray()).join()

        tasks.forEach {
            val data = it.join()
            data.second.forEachIndexed { i, pixel ->
                pixelIndex = data.first + i
                val rgbIndex = pixelIndex * 3
                alphaChannel[pixelIndex] = Byte.MAX_VALUE // (pixel.w * 255).roundToInt().toByte()
                if (pixel.w <= 1.0) {
                    val x = (pixelIndex % width) / 8
                    val y = (pixelIndex / width) / 8
                    val transparencyGrid = if ((x + y) % 2 == 0) 0.4 else 0.2
                    pixel.x = pixel.x * pixel.w + transparencyGrid * (1 - pixel.w)
                    pixel.y = pixel.y * pixel.w + transparencyGrid * (1 - pixel.w)
                    pixel.z = pixel.z * pixel.w + transparencyGrid * (1 - pixel.w)
                }
                sampleBuffer[rgbIndex + 0] = pixel.x
                sampleBuffer[rgbIndex + 1] = pixel.y
                sampleBuffer[rgbIndex + 2] = pixel.z
            }
        }

        scene.spp = 1
        postFrameUpdateCallback()
    }
}
