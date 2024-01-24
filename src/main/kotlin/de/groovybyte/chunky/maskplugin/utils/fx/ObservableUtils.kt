package de.groovybyte.chunky.maskplugin.utils.fx

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.value.ObservableValue

/**
 * @author Maximilian Stiede
 */
fun <T : Any?> ObservableValue<T>.onChange(callback: (T) -> Unit) {
    this.addListener { _, _, newValue -> callback(newValue) }
}

fun <S : Any?, T : Any?> ObservableValue<S>.mapBinding(mapping: (S) -> T): ObjectBinding<T> =
    Bindings.createObjectBinding(
        { mapping(this.value) },
        this
    )

fun String.asReadOnlyProperty(): ReadOnlyStringWrapper = ReadOnlyStringWrapper(this)
fun <T : Any> T.asReadOnlyProperty(): ReadOnlyProperty<T> = ReadOnlyObjectWrapper(this)
