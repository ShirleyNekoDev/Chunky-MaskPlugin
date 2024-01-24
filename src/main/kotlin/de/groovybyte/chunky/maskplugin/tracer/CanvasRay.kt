package de.groovybyte.chunky.maskplugin.tracer

import se.llbit.math.Ray
import se.llbit.math.Vector2

class CanvasRay(
    val canvasPos: Vector2,
    val canvasPixelPos: Vec2i,
) : Ray() {
    constructor(
        x: Double, y: Double,
        px: Int, py: Int
    ) : this(
        Vector2(x, y),
        Vec2i(px, py)
    )
    
    data class Vec2i(
        val x: Int,
        val y: Int,
    )
}
