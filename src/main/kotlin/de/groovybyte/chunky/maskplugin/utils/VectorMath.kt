package de.groovybyte.chunky.maskplugin.utils

import se.llbit.math.Vector4

/**
 * @author Maximilian Stiede
 */

operator fun Vector4.plusAssign(other: Vector4) {
    x += other.x
    y += other.y
    z += other.z
    w += other.w
}

operator fun Vector4.divAssign(divisor: Double) {
    x /= divisor
    y /= divisor
    z /= divisor
    w /= divisor
}
