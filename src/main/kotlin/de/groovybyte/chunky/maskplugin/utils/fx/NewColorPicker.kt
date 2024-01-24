package de.groovybyte.chunky.maskplugin.utils.fx

import javafx.beans.property.Property
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import javafx.stage.Popup

/**
 * like [se.llbit.fx.LuxColorPicker] but different™️
 */
class NewColorPicker(
    val color: Property<Color>,
    val originalColor: Color = color.value,
) : Button() {
    init {
        paddingAll = 0.0
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE
        backgroundProperty().bind(color.mapBinding {
            Background(
                BackgroundFill(
                    it,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
                )
            )
        })
        action {
//            colorPi
        }
    }

    companion object {
        val popup = Popup()
    }

    @Synchronized
    fun openPicker() {

    }
}
