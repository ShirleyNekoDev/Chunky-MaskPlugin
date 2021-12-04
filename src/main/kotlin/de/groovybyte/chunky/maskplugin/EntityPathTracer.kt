package de.groovybyte.chunky.maskplugin

import de.groovybyte.chunky.maskplugin.utils.getSafeFieldGetterFor
import se.llbit.chunky.block.Air
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.math.BVH
import se.llbit.math.Ray
import se.llbit.math.Vector4

/**
 * @author Maximilian Stiede
 */
object EntityPathTracer {
    private val getBVH = Scene::class.java
        .getSafeFieldGetterFor<Scene, BVH>("bvh")
    private val getActorBVH = Scene::class.java
        .getSafeFieldGetterFor<Scene, BVH>("actorBvh")

    fun trace(x: Int, y: Int, scene: Scene): Vector4 {
        val halfWidth = scene.width / (2.0 * scene.height)
        val invHeight = 1.0 / scene.height

        val ray = Ray()
        scene.camera().calcViewRay(
            ray,
            -halfWidth + x * invHeight,
            -.5 + y * invHeight
        )
        ray.o.sub(scene.origin)
        trace(scene, ray)

        return ray.color
    }

    fun trace(scene: Scene, ray: Ray) {
        while (true) {
            if (!nextIntersection(scene, ray)) {
                break
            } else if (ray.currentMaterial !== Air.INSTANCE && ray.color.w > 0) {
                break
            } else {
                ray.o.scaleAdd(Ray.OFFSET, ray.d)
            }
        }
    }

    fun nextIntersection(scene: Scene, ray: Ray): Boolean {
        ray.t = Double.POSITIVE_INFINITY
        var hit = false

        if (scene.getBVH().closestIntersection(ray)) {
//            ray.color.set(Vector4(1.0, 0.0, 0.0, 1.0))
            hit = true
        }
        if (scene.getActorBVH().closestIntersection(ray)) {
//            ray.color.set(Vector4(1.0, 0.0, 0.0, 1.0))
            hit = true
        }

        if (hit) {
            ray.distance += ray.t
            ray.o.scaleAdd(ray.t, ray.d)
            return true
        } else {
            ray.currentMaterial = Air.INSTANCE
            return false
        }
    }
}
