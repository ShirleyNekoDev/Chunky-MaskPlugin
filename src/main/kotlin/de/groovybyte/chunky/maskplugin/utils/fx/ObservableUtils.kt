package de.groovybyte.chunky.maskplugin.utils.fx

import javafx.beans.value.ObservableValue

/**
 * @author Maximilian Stiede
 */
fun <T : Any?> ObservableValue<T>.onChange(callback: (T?) -> Unit) {
    this.addListener { _, _, newValue -> callback(newValue) }
}
