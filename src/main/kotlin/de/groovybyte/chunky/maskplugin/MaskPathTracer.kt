package de.groovybyte.chunky.maskplugin

import de.groovybyte.chunky.maskplugin.utils.getSafeFieldGetterFor
import se.llbit.chunky.block.Air
import se.llbit.chunky.block.Water
import se.llbit.chunky.renderer.WorkerState
import se.llbit.chunky.renderer.scene.RayTracer
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.math.Ray
import se.llbit.math.Vector2
import se.llbit.math.Vector4
import se.llbit.math.bvh.BVH

/**
 * @author Maximilian Stiede
 */
class MaskPathTracer(
    val skyMaskColor: Vector4 = Vector4(0.0, 0.0, 0.0, 1.0),
    val cloudMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val waterMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val bvhMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val actorMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val anyMaterialMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val specificMaterialMaskColors: MutableMap<String, Vector4> = HashMap(),
) : RayTracer {
//    private var maskBVH: BVH

    private val getBVH = Scene::class.java
        .getSafeFieldGetterFor<Scene, BVH>("bvh")
    private val shouldRenderActors = Scene::class.java
        .getSafeFieldGetterFor<Scene, Boolean>("renderActors")
    private val getActorBVH = Scene::class.java
        .getSafeFieldGetterFor<Scene, BVH>("actorBvh")

    // TODO
    private val superSamplingOffsets = arrayOf(
        // 2x2 rotated grid
//        Vector2(-3.0 / 8.0, +1.0/ 8.0),
//        Vector2(+1.0 / 8.0, +3.0/ 8.0),
//        Vector2(-1.0 / 8.0, -3.0/ 8.0),
//        Vector2(+3.0 / 8.0, -1.0/ 8.0),
        // no supersampling
        Vector2(0.0, 0.0),
        // 2x2 grid
//        Vector2(-1.0 / 4.0, -1.0 / 4.0),
//        Vector2(1.0 / 4.0, -1.0 / 4.0),
//        Vector2(-1.0 / 4.0, 1.0 / 4.0),
//        Vector2(1.0 / 4.0, 1.0 / 4.0),
    )

    private operator fun Vector4.plusAssign(other: Vector4) {
        x += other.x
        y += other.y
        z += other.z
        w += other.w
    }

    private operator fun Vector4.divAssign(divisor: Double) {
        x /= divisor
        y /= divisor
        z /= divisor
        w /= divisor
    }

    fun rotatedGridSupersampling(x: Int, y: Int, scene: Scene): Vector4 {
        val halfWidth = scene.width / (2.0 * scene.height)
        val invHeight = 1.0 / scene.height

        val ray = Ray()
        val color = Vector4()

        superSamplingOffsets.forEach { offset ->
            scene.camera().calcViewRay(
                ray,
//                x * 1.0 / scene.width - 0.5,
//                y * 1.0 / scene.height - 0.5
                -halfWidth + (x + offset.x) * invHeight,
                -.5 + (y + offset.y) * invHeight
            )
            ray.o.sub(scene.origin)
            trace(scene, ray)
            color += ray.color
        }
        color /= superSamplingOffsets.size.toDouble()

        return color
    }

    /**
     * Find next ray intersection.
     * @return Next intersection
     */
    fun nextIntersection(scene: Scene, ray: Ray): Boolean {
        ray.setPrevMaterial(ray.currentMaterial, ray.currentData)
        ray.t = Double.POSITIVE_INFINITY
        var hit = false
        if (scene.sky().cloudsEnabled()) {
            if (scene.sky().cloudIntersection(scene, ray)) {
                hit = true
                ray.color.set(cloudMaskColor)
            }
        }
        if (scene.isWaterPlaneEnabled) {
            if (ray.d.y < 0) {
                val t = (scene.effectiveWaterPlaneHeight - ray.o.y - scene.origin.y) / ray.d.y
                if (t > 0 && t < ray.t) {
                    ray.t = t
                    ray.color.set(waterMaskColor)
                    ray.n[0.0, 1.0] = 0.0
                    ray.currentMaterial = Water.INSTANCE
                    hit = true
                }
            }
            if (ray.d.y > 0) {
                val t = (scene.effectiveWaterPlaneHeight - ray.o.y - scene.origin.y) / ray.d.y
                if (t > 0 && t < ray.t) {
                    ray.t = t
                    ray.color.set(waterMaskColor)
                    ray.n[0.0, -1.0] = 0.0
                    ray.currentMaterial = Air.INSTANCE
                    hit = true
                }
            }
        }

        if (scene.getBVH().closestIntersection(ray)) {
            ray.color.set(bvhMaskColor)
            hit = true
        }
        if (scene.shouldRenderActors()) {
            if (scene.getActorBVH().closestIntersection(ray)) {
                ray.color.set(actorMaskColor)
                hit = true
            }
        }

        val start3 = Ray(ray)
        start3.setCurrentMaterial(ray.prevMaterial, ray.prevData)
        var r3 = Ray(start3)
        r3.setCurrentMaterial(start3.prevMaterial, start3.prevData)
        if (scene.worldOctree.enterBlock(scene, r3, scene.palette) && r3.distance < ray.t) {
            ray.t = r3.distance
            ray.n.set(r3.n)
            ray.color.set(
                specificMaterialMaskColors.getOrDefault(
                    r3.currentMaterial.name,
                    anyMaterialMaskColor
                )
            )
            ray.setPrevMaterial(r3.prevMaterial, r3.prevData)
            ray.setCurrentMaterial(r3.currentMaterial, r3.currentData)
            hit = true
        }
        if (start3.currentMaterial.isWater) {
            if (start3.currentMaterial !== Water.INSTANCE) {
                r3 = Ray(start3)
                r3.setCurrentMaterial(start3.prevMaterial, start3.prevData)
                if (
                    scene.waterOctree.exitWater(scene, r3, scene.palette)
                    && r3.distance < ray.t - Ray.EPSILON
                ) {
                    ray.t = r3.distance
                    ray.n.set(r3.n)
                    ray.color.set(
                        specificMaterialMaskColors.getOrDefault(
                            r3.currentMaterial.name,
                            anyMaterialMaskColor
                        )
                    )
                    ray.setPrevMaterial(r3.prevMaterial, r3.prevData)
                    ray.setCurrentMaterial(r3.currentMaterial, r3.currentData)
                    hit = true
                } else if (ray.prevMaterial === Air.INSTANCE) {
                    ray.setPrevMaterial(Water.INSTANCE, 1 shl Water.FULL_BLOCK)
                }
            }
        } else {
            r3 = Ray(start3)
            r3.setCurrentMaterial(start3.prevMaterial, start3.prevData)
            if (scene.waterOctree.enterBlock(scene, r3, scene.palette) && r3.distance < ray.t) {
                ray.t = r3.distance
                ray.n.set(r3.n)
                ray.color.set(waterMaskColor)
                ray.setPrevMaterial(r3.prevMaterial, r3.prevData)
                ray.setCurrentMaterial(r3.currentMaterial, r3.currentData)
                hit = true
            }
        }

        if (hit) {
            ray.distance += ray.t
            ray.o.scaleAdd(ray.t, ray.d)
            if (ray.currentMaterial.isWater || (ray.currentMaterial === Air.INSTANCE && ray.prevMaterial.isWater)) {
                ray.color.set(waterMaskColor)
            }
            return true
        } else {
            ray.currentMaterial = Air.INSTANCE
            return false
        }
    }

    fun trace(scene: Scene, ray: Ray) {
        if (scene.isInWater(ray)) {
            ray.currentMaterial = Water.INSTANCE
        } else {
            ray.currentMaterial = Air.INSTANCE
        }
        while (true) {
            if (!nextIntersection(scene, ray)) {
                break
            } else if (ray.currentMaterial !== Air.INSTANCE && ray.color.w > 0) {
                break
            } else {
                ray.o.scaleAdd(Ray.OFFSET, ray.d)
            }
        }

        if (ray.currentMaterial === Air.INSTANCE) {
            ray.color.set(skyMaskColor)
        }
    }

    override fun trace(scene: Scene, state: WorkerState) {
        trace(scene, state.ray)
    }
}
