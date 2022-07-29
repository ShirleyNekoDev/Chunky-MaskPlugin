package de.groovybyte.chunky.maskplugin.utils.fx

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.*

fun <N : Node> Pane.add(child: N, op: N.() -> Unit = {}) {
    children.add(child.apply(op))
}

//fun HBox.fillWidth() {
//    childrenUnmodifiable.forEach { child ->
//        child.hgrow = Priority.ALWAYS
//        if (child is Region) {
//            child.maxWidth = Double.POSITIVE_INFINITY
//        }
//    }
//}

//fun <T> TableView<T>.fixColumnWidths() {
//    runLater(Duration(30.0)) {
//        lookupAll(".column-header")
//            .filterIsInstance<Parent>()
//            .forEach(javafx.scene.Parent::requestLayout)
//    }
//}

fun Button.action(callback: Button.() -> Unit) = setOnAction { callback() }
