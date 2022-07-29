package de.groovybyte.chunky.maskplugin.utils.fx

import javafx.geometry.Insets
import javafx.scene.layout.*
import javafx.scene.paint.Color

/**
 * @author Maximilian Stiede
 */
val DEBUG_BORDER = Border(
    BorderStroke(
        Color.RED,
        BorderStrokeStyle.SOLID,
        CornerRadii.EMPTY,
        BorderStroke.THIN
    )
)

fun Region.debugBorder(): Border = DEBUG_BORDER.also { border = it }

fun Region.border(
    color: Color = Color.BLACK,
    insets: Insets = Insets.EMPTY,
    strokeWidth: BorderWidths = BorderStroke.THIN,
    strokeStyle: BorderStrokeStyle = BorderStrokeStyle.SOLID,
    cornerRadii: CornerRadii = CornerRadii.EMPTY
): Border = Border(
    BorderStroke(
        color,
        strokeStyle,
        cornerRadii,
        strokeWidth,
        insets
    )
).also {
    border = it
}

fun borderWidth(
    top: Number,
    right: Number = top,
    bottom: Number = top,
    left: Number = top
): BorderWidths = BorderWidths(
    top.toDouble(), right.toDouble(), bottom.toDouble(), left.toDouble()
)
