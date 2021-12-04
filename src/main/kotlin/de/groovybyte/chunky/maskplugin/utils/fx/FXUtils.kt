package de.groovybyte.chunky.chunkycloudplugin.utils

import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.TableView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.util.Duration
import tornadofx.*

fun <N : Node> EventTarget.add(child: N, op: N.() -> Unit) {
    add(child.apply(op))
}

public val DEBUG_BORDER = Border(
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

fun HBox.fillWidth() {
    childrenUnmodifiable.forEach { child ->
        child.hgrow = Priority.ALWAYS
        if (child is Region) {
            child.maxWidth = Double.POSITIVE_INFINITY
        }
    }
}

fun <T> TableView<T>.fixColumnWidths() {
    runLater(Duration(30.0)) {
        lookupAll(".column-header")
            .filterIsInstance<Parent>()
            .forEach(javafx.scene.Parent::requestLayout)
    }
}
