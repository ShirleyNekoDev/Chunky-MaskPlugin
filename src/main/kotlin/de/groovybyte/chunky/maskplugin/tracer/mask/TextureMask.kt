package de.groovybyte.chunky.maskplugin.tracer.mask

import de.groovybyte.chunky.maskplugin.tracer.CanvasRay
import se.llbit.chunky.resources.Texture

class TextureMask(
    var texture: Texture,
    val textureFilter: TextureFilter = TextureFilter.ALPHA,
) : Mask {
    @FunctionalInterface
    interface TextureFilter {
        fun accept(r: Float, g: Float, b: Float, a: Float): Boolean

        companion object {
            val ALPHA = object : TextureFilter {
                override fun accept(r: Float, g: Float, b: Float, a: Float): Boolean
                    = a > 0
            }
            val NOT_BLACK = object : TextureFilter {
                override fun accept(r: Float, g: Float, b: Float, a: Float): Boolean
                    = r > 0 && g > 0 && b > 0
            }
        }
    }

    override fun acceptRay(ray: CanvasRay): Boolean {
        val (r, g, b, a) = texture.getColor(ray.canvasPos.x, 1.0 - ray.canvasPos.y)
        return textureFilter.accept(r, g, b, a)
    }
}
