package de.groovybyte.chunky.maskplugin.tracer

import de.groovybyte.chunky.maskplugin.FakeMaterial
import de.groovybyte.chunky.maskplugin.MaskColorConfiguration
import de.groovybyte.chunky.maskplugin.utils.CelestialUtils
import de.groovybyte.chunky.maskplugin.utils.*
import se.llbit.chunky.block.minecraft.Air
import se.llbit.chunky.block.minecraft.Water
import se.llbit.chunky.model.minecraft.WaterModel
import se.llbit.chunky.renderer.WorkerState
import se.llbit.chunky.renderer.scene.RayTracer
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.math.Ray
import se.llbit.math.Vector4

/**
 * @author Maximilian Stiede
 */
open class MaskRayTracer(
    val colorConfig: MaskColorConfiguration
) : RayTracer {

    override fun trace(scene: Scene, state: WorkerState) {
        traceRay(scene, state.ray)
    }

    fun traceRay(scene: Scene, ray: Ray) {
        // TODO(here or in maskRenderer): apply fixes from Scene::rayTrace

        if (scene.isInWater(ray)) {
            ray.currentMaterial = Water.INSTANCE
        } else {
            ray.currentMaterial = Air.INSTANCE
        }

        followRay(scene, ray)

        if (ray.currentMaterial === Air.INSTANCE) {
            ray.color.set(colorConfig.skyMaskColor)
        }

//        if (ray.currentMaterial.isWater) {
//            traceWaterMask(scene, ray)
//        }
    }

    fun followRay(scene: Scene, ray: Ray): Boolean {
        while (true) {
            if (!nextIntersection(scene, ray)) {
                return false
            } else if (ray.currentMaterial !== Air.INSTANCE && ray.color.w > 0) {
                return true
            } else {
                ray.o.scaleAdd(Ray.OFFSET, ray.d)
            }
        }
    }

//    private var maskBVH: BVH

    companion object {
        private val SUN_MATERIAL = FakeMaterial("sun")
    }

    /*
    sun
    water reflections (sky vs blocks vs sun vs clouds)
    shadows vs direct sunlight
    light source distance
    normal/geometrynormal difference
    diffuse, specular, etc. material properties
    orthogonal/real water depth
    ray length in water
    distance to corner (Screen Space Ambient Occlusion vs Ray-traced ambient occlusion)


    tree view -> selection set color
    preview material texture
    */

    fun nextIntersection(scene: Scene, ray: Ray): Boolean {
        ray.setPrevMaterial(ray.currentMaterial, ray.currentData)
        ray.t = Double.POSITIVE_INFINITY
        var hit = false
        if (scene.sky().cloudsEnabled()) {
            if (scene.sky().cloudIntersection(scene, ray)) {
                hit = true
                ray.color.set(colorConfig.cloudMaskColor)
            }
        }
        if (scene.isWaterPlaneEnabled) {
            if (ray.d.y < 0) {
                val t = (scene.effectiveWaterPlaneHeight - ray.o.y - scene.origin.y) / ray.d.y
                if (t > 0 && t < ray.t) {
                    ray.t = t
                    ray.color.set(colorConfig.waterMaskColor)
                    ray.setNormal(0.0, 1.0, 0.0)
                    ray.currentMaterial = Water.INSTANCE
                    hit = true
                    // check water depth and color water accordingly
                    return false // TODO
                }
            }
            if (ray.d.y > 0) {
                val t = (scene.effectiveWaterPlaneHeight - ray.o.y - scene.origin.y) / ray.d.y
                if (t > 0 && t < ray.t) {
                    ray.t = t
                    ray.color.set(colorConfig.waterMaskColor)
                    ray.setNormal(0.0, -1.0, 0.0)
                    ray.currentMaterial = Air.INSTANCE
                    hit = true
                }
            }
        }

        if (scene.getSceneEntities().getBVH().closestIntersection(ray)) {
            ray.color.set(colorConfig.bvhMaskColor)
            hit = true
        }
        if (scene.getSceneEntities().shouldRenderActors()) {
            if (scene.getSceneEntities().getActorBVH().closestIntersection(ray)) {
                ray.color.set(colorConfig.actorMaskColor)
                hit = true
            }
        }

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
            ray.color.set(
                colorConfig.specificMaterialMaskColors.getOrDefault(
                    r3.currentMaterial.name,
                    colorConfig.anyMaterialMaskColor
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
                    ray.normal = r3.normal
                    ray.color.set(
                        colorConfig.specificMaterialMaskColors.getOrDefault(
                            r3.currentMaterial.name,
                            colorConfig.anyMaterialMaskColor
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
            if (
                scene.waterOctree.enterBlock(scene, r3, scene.palette)
                && r3.distance < ray.t
            ) {
                ray.t = r3.distance
                ray.normal = r3.normal
                ray.color.set(colorConfig.waterMaskColor)
                ray.setPrevMaterial(r3.prevMaterial, r3.prevData)
                ray.setCurrentMaterial(r3.currentMaterial, r3.currentData)
                hit = true
            }
        }

        if (hit) {
            ray.distance += ray.t
            ray.o.scaleAdd(ray.t, ray.d)
            if (ray.currentMaterial.isWater || (ray.currentMaterial === Air.INSTANCE && ray.prevMaterial.isWater)) {
                ray.color.set(colorConfig.waterMaskColor)
            }
            return true
        } else {
//            if(scene.sun().intersect(ray)) {
            if(CelestialUtils.intersectsSun(ray, scene.sun())) {
                ray.color.set(colorConfig.sunMaskColor)
                ray.currentMaterial = SUN_MATERIAL
                return true
            } else {
                ray.currentMaterial = Air.INSTANCE
            }
            return false
        }
    }

    private val noMaskColor: Vector4 = Vector4(0.0, 0.0, 0.0, 1.0)
    private val maskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0)

    private val skyReflectionColor: Vector4 = Vector4(0.0, 0.0, 1.0, 1.0)
    private val blockReflectionColor: Vector4 = Vector4(1.0, 0.0, 0.0, 1.0)

//    protected fun traceWaterMask(scene: Scene, ray: Ray) {
//        // water mask hit
//
//        // TODO: Scene::getCurrentWaterShader(), Scene::getWaterShader(wss), WaterShadingStrategy
//        if (!scene.stillWaterEnabled() && ray.normal.y != 0.0) {
//            WaterModel.doWaterDisplacement(ray)
//        }
//
//        // do specular reflection
//        val reflected = Ray();
//        reflected.specularReflection(ray, null) // water does not have roughness (please)
//        if (!followRay(scene, reflected)) {
//            ray.color.set(skyReflectionColor)
//        } else {
//            ray.color.set(blockReflectionColor)
//        }
//    }
}
