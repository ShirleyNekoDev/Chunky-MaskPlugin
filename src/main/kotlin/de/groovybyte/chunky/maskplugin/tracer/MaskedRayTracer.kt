package de.groovybyte.chunky.maskplugin.tracer

import de.groovybyte.chunky.maskplugin.tracer.mask.Mask
import se.llbit.chunky.renderer.WorkerState
import se.llbit.chunky.renderer.scene.RayTracer
import se.llbit.chunky.renderer.scene.Scene

/**
 * @author Maximilian Stiede
 */
class MaskedRayTracer(
    var mask: Mask,
    val parentRayTracer: RayTracer,
) : RayTracer {

    override fun trace(scene: Scene, state: WorkerState) {
        val ray = state.ray
        if(ray !is CanvasRay || mask.acceptRay(ray))
            parentRayTracer.trace(scene, state)
    }
}
