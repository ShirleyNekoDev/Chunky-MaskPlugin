package de.groovybyte.chunky.maskplugin.utils.fx

import javafx.geometry.Insets
import javafx.scene.layout.Region

/**
 * @author Maximilian Stiede
 */
var Region.paddingTop
    get(): Double = padding.top
    set(value: Double) { padding = Insets(value, paddingRight, paddingBottom, paddingLeft) }
var Region.paddingRight
    get(): Double = padding.right
    set(value: Double) { padding = Insets(paddingTop, value, paddingBottom, paddingLeft) }
var Region.paddingBottom
    get(): Double = padding.top
    set(value: Double) { padding = Insets(paddingTop, paddingRight, value, paddingLeft) }
var Region.paddingLeft
    get(): Double = padding.top
    set(value: Double) { padding = Insets(paddingTop, paddingRight, paddingBottom, value) }
var Region.paddingAll
    get(): Double = listOf(paddingTop, paddingRight, paddingBottom, paddingLeft).max()
    set(value: Double) { padding = Insets(value) }
