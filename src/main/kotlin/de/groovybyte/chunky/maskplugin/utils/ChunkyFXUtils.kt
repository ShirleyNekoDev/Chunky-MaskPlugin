package de.groovybyte.chunky.maskplugin.utils

import se.llbit.chunky.ui.RenderControlsFxController
import se.llbit.chunky.ui.render.RenderControlsTab

/**
 * @author Maximilian Stiede
 */
inline fun <reified T> RenderControlsFxController.findTabOfType() = this
    .getSafeFromField<Collection<RenderControlsTab>>("tabs")
    .filterIsInstance<T>()
    .first()
