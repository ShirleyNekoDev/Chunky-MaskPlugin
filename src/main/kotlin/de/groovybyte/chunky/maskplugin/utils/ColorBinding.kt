package de.groovybyte.chunky.maskplugin.utils

import de.groovybyte.chunky.maskplugin.utils.fx.onChange
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color
import se.llbit.math.Vector4

/**
 * @author Maximilian Stiede
 */
class ColorBinding(colorRef: Vector4) {
    private val defaultColor = colorRef.toColor()

    val color = SimpleObjectProperty(colorRef.toColor()).apply {
        onChange { colorRef.set(it!!) }
    }

    fun setToDefault() {
        color.value = defaultColor
    }

    fun onChange(callback: (Color) -> Unit) {
        color.onChange {
            if (it != null && it != defaultColor) {
                callback(it)
            }
        }
    }
}
