package de.groovybyte.chunky.maskplugin

import de.groovybyte.chunky.maskplugin.math.SuperSampling
import de.groovybyte.chunky.maskplugin.ui.MaskConfigTab
import se.llbit.chunky.Plugin
import se.llbit.chunky.main.Chunky
import se.llbit.chunky.main.ChunkyOptions
import se.llbit.chunky.ui.ChunkyFx
import se.llbit.chunky.ui.render.RenderControlsTabTransformer

/**
 * @author Maximilian Stiede
 */
class MaskPlugin : Plugin {
    companion object {
        const val NAME = "MaskPlugin"

        private val colorConfig = MaskColorConfiguration()

        private val maskPathTracer = MaskRayTracer(colorConfig)
        val previewMaskRenderer = SuperSampling1FrameRenderer(
            "MaskPreviewRenderer",
            SuperSampling.NONE,
            maskPathTracer,
            colorConfig::updateBlockPalette
        )
        val renderMaskRenderer = SuperSampling1FrameRenderer(
            "MaskRenderer",
            SuperSampling.ROTATED_GRID_2x2,
            maskPathTracer,
            colorConfig::updateBlockPalette
        )
    }

    override fun attach(chunky: Chunky) {
        Chunky.addPreviewRenderer(previewMaskRenderer)
        Chunky.addRenderer(renderMaskRenderer)

        if (!chunky.isHeadless) {
            val oldTransformer: RenderControlsTabTransformer = chunky.renderControlsTabTransformer
            chunky.renderControlsTabTransformer = RenderControlsTabTransformer { tabs ->
                oldTransformer
                    .apply(tabs)
                    .apply {
                        add(MaskConfigTab(chunky, colorConfig))
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
