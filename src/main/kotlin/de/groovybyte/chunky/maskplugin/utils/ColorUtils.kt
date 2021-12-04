package de.groovybyte.chunky.maskplugin.utils

import javafx.scene.paint.Color
import se.llbit.chunky.world.Material
import se.llbit.math.Vector4

/**
 * @author Maximilian Stiede
 */

fun Vector4.toColor(): Color = Color(x, y, z, w)
fun Vector4.set(color: Color) = with(color) { set(red, green, blue, opacity) }

fun Color.copy(
    red: Double = this.red,
    green: Double = this.green,
    blue: Double = this.blue,
    opacity: Double = this.opacity
) = Color(
    red,
    green,
    blue,
    opacity
)

val Material.averageFlatColor
    get() = Vector4().also {
        it.set(texture.avgColorLinear)
    }
