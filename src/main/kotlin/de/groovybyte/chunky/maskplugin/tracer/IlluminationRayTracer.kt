package de.groovybyte.chunky.maskplugin.tracer

import se.llbit.chunky.renderer.WorkerState
import se.llbit.chunky.renderer.scene.RayTracer
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.math.ColorUtil
import se.llbit.math.Ray

/**
 * @author Maximilian Stiede
 */
object IlluminationRayTracer : RayTracer {
    override fun trace(scene: Scene, state: WorkerState) {
        traceRay(scene, state.ray)
    }

    fun traceRay(scene: Scene, ray: Ray) {
        ray.t = Double.POSITIVE_INFINITY

        val start3 = Ray(ray)
        start3.setCurrentMaterial(ray.prevMaterial, ray.prevData)
        var r3 = Ray(start3)
        r3.setCurrentMaterial(start3.prevMaterial, start3.prevData)
        if (
            scene.worldOctree.enterBlock(scene, r3, scene.palette)
            && r3.distance < ray.t
        ) {
            ray.t = r3.distance
            ray.normal = r3.normal
            ray.color.set(r3.color)

            val min = 2 // red = 1
            val max = 10 // red = 0
            ray.color.x = when {
                ray.t >= max -> 0.0
                ray.t <= min -> 1.0
                else -> 1.0 - (ray.t - min) / (max - min)
            }

//            r3.currentMaterial.texture.getAvgColorLinear(ray.color)
//            ColorUtil.getRGBComponents(r3.currentMaterial.texture.avgColor, ray.color)

            ray.setPrevMaterial(r3.prevMaterial, r3.prevData)
            ray.setCurrentMaterial(r3.currentMaterial, r3.currentData)
        }
    }
}
