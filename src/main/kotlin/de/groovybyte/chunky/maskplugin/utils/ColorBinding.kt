package de.groovybyte.chunky.maskplugin.utils

import de.groovybyte.chunky.maskplugin.utils.fx.onChange
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color
import se.llbit.math.Vector4

/**
 * @author Maximilian Stiede
 */
open class ColorBinding(
    colorRef: Vector4,
    val color: SimpleObjectProperty<Color> = SimpleObjectProperty(colorRef.toColor())
) : ObservableValue<Color> by color {
    private val defaultColor = colorRef.toColor()

    init {
        color.onChange { colorRef.set(it!!) }
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

class NamedColorBinding(
    val name: String,
    colorRef: Vector4
) : ColorBinding(colorRef), ObservableValue<Color> {

    init {
        color.onChange { println(this) }
    }

    override fun toString(): String = "$name->${color.value}"
}
