package de.groovybyte.chunky.maskplugin.utils

import se.llbit.chunky.renderer.scene.Sun
import se.llbit.math.Ray
import se.llbit.math.Vector3
import kotlin.math.acos

/**
 * @author Maximilian Stiede
 */
object CelestialUtils {
//    companion object {
        private val getSW = Sun::class.java
            .getSafeFieldGetterFor<Sun, Vector3>("sw")
        private val getSU = Sun::class.java
            .getSafeFieldGetterFor<Sun, Vector3>("su")
        private val getSV = Sun::class.java
            .getSafeFieldGetterFor<Sun, Vector3>("sv")
//    }

    val Sun.location: Vector3 get() = this.getSW()

    fun intersectsSun(ray: Ray, sun: Sun): Boolean {
        val sunLocation = sun.location
        if(ray.d.dot(sunLocation) < .5) return false

        val WIDTH = sun.radius * 4
        val WIDTH2 = WIDTH * 2
        var a: Double = Math.PI / 2 - acos(ray.d.dot(sun.getSU())) + WIDTH
        if (a >= 0 && a < WIDTH2) {
            val b = Math.PI / 2 - acos(ray.d.dot(sun.getSV())) + WIDTH
            if (b >= 0 && b < WIDTH2) {
//                Sun.texture.getColor(a / WIDTH2, b / WIDTH2, ray.color)
//                ray.color.x *= emittance.x * 10
//                ray.color.y *= emittance.y * 10
//                ray.color.z *= emittance.z * 10
                return true
            }
        }

        return false
    }
}
