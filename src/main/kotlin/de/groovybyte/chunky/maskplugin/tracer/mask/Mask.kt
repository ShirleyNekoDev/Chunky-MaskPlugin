package de.groovybyte.chunky.maskplugin.tracer.mask

import de.groovybyte.chunky.maskplugin.tracer.CanvasRay

@FunctionalInterface
interface Mask {
    fun acceptRay(ray: CanvasRay): Boolean
}
