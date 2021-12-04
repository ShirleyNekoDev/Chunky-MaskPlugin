package de.groovybyte.chunky.maskplugin

import de.groovybyte.chunky.maskplugin.ui.MaskConfigTab
import se.llbit.chunky.Plugin
import se.llbit.chunky.main.Chunky
import se.llbit.chunky.main.ChunkyOptions
import se.llbit.chunky.ui.ChunkyFx
import se.llbit.chunky.ui.render.RenderControlsTabTransformer

/**
 * @author Maximilian Stiede
 */
class MagicTexturesPlugin : Plugin {
    companion object {
        const val NAME = "MaskPlugin"
    }

    override fun attach(chunky: Chunky) {
        chunky.setRendererFactory(::SwitchableRenderManager)

        if (!chunky.isHeadless) {
            val oldTransformer: RenderControlsTabTransformer = chunky.renderControlsTabTransformer
            chunky.renderControlsTabTransformer = RenderControlsTabTransformer { tabs ->
                oldTransformer
                    .apply(tabs)
                    .apply {
                        add(MaskConfigTab(chunky))
                    }
            }
        }
    }
}

fun main() {
    // Start Chunky normally with this plugin attached.
    Chunky.loadDefaultTextures()
    val chunky = Chunky(ChunkyOptions.getDefaults())
    MagicTexturesPlugin().attach(chunky)
    ChunkyFx.startChunkyUI(chunky)
}
