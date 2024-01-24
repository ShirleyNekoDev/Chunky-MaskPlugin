package de.groovybyte.chunky.maskplugin

import de.groovybyte.chunky.maskplugin.math.SuperSampling
import de.groovybyte.chunky.maskplugin.tracer.EntityRayTracer
import de.groovybyte.chunky.maskplugin.tracer.ExtendedPathTracingRenderer
import de.groovybyte.chunky.maskplugin.tracer.ExtendedPreviewRenderer
import de.groovybyte.chunky.maskplugin.tracer.MaskRayTracer
import de.groovybyte.chunky.maskplugin.tracer.MaskedRayTracer
import de.groovybyte.chunky.maskplugin.tracer.mask.TextureMask
import de.groovybyte.chunky.maskplugin.ui.MaskConfigTab
import se.llbit.chunky.Plugin
import se.llbit.chunky.main.Chunky
import se.llbit.chunky.main.ChunkyOptions
import se.llbit.chunky.renderer.scene.PathTracer
import se.llbit.chunky.renderer.scene.PreviewRayTracer
import se.llbit.chunky.resources.Texture
import se.llbit.chunky.ui.ChunkyFx
import se.llbit.chunky.ui.render.RenderControlsTabTransformer
import se.llbit.resources.ImageLoader
import java.io.File

/**
 * @author Maximilian Stiede
 */
class MaskPlugin : Plugin {
    companion object {
        private val colorConfig = MaskColorConfiguration()

        private val maskPathTracer = MaskRayTracer(colorConfig)
    }

    override fun attach(chunky: Chunky) {
//        Chunky.addPreviewRenderer(SuperSampling1FrameRenderer(
//            "MaskPreviewRenderer",
//            SuperSampling.NONE,
//            maskPathTracer,
//            colorConfig::updateBlockPalette
//        ))
//        Chunky.addRenderer(SuperSampling1FrameRenderer(
//            "MaskRenderer",
//            SuperSampling.ROTATED_GRID_2x2,
//            maskPathTracer,
//            colorConfig::updateBlockPalette
//        ))
//        Chunky.addPreviewRenderer(Garbage(IlluminationRayTracer))

//        val mask = object : Mask {
//            override fun acceptRay(ray: CanvasRay): Boolean {
//                return (ray.canvasPos.x < 0.5) == (ray.canvasPos.y < 0.5)
////                return (ray.canvasPixelPos.x and 0b1000) == (ray.canvasPixelPos.y and 0b1000)
//            }
//        }
        val mask = TextureMask(
            Texture(ImageLoader.read(
                File("D:\\.chunky\\scenes\\sky_mask_test\\snapshots\\sky_mask_test-5.png")
            )),
            TextureMask.TextureFilter.ALPHA
        )
        Chunky.addPreviewRenderer(ExtendedPreviewRenderer(
            "MaskedPreviewRayTracer",
            "MaskedPreviewRayTracer",
            MaskedRayTracer(
                mask,
                PreviewRayTracer()
            )
        ))
        Chunky.addRenderer(ExtendedPathTracingRenderer(
            "MaskedRayTracer",
            "MaskedRayTracer",
            "",
            MaskedRayTracer(
                mask,
                PathTracer()
            )
        ))

        Chunky.addPreviewRenderer(SuperSampling1FrameRenderer(
            "EntityMaskPreviewRenderer",
            SuperSampling.NONE,
            EntityRayTracer
        ))
//        Chunky.addRenderer(SuperSampling1FrameRenderer(
//            "EntityMaskRenderer",
//            SuperSampling.ROTATED_GRID_2x2,
//            EntityRayTracer
//        ))

        if (!chunky.isHeadless) {
            val oldTransformer: RenderControlsTabTransformer = chunky.renderControlsTabTransformer
            chunky.renderControlsTabTransformer = RenderControlsTabTransformer { tabs ->
                oldTransformer
                    .apply(tabs)
                    .apply {
                        add(MaskConfigTab(chunky, colorConfig))
//                        add(AnaglyphTab(chunky))
                    }
            }
        }
    }
}

fun main() {
    // Start Chunky normally with this plugin attached.
    Chunky.loadDefaultTextures()
    val chunky = Chunky(ChunkyOptions.getDefaults())
    MaskPlugin().attach(chunky)
    ChunkyFx.startChunkyUI(chunky)
}
