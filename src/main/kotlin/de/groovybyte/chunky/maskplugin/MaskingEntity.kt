package de.groovybyte.chunky.maskplugin

import se.llbit.chunky.entity.Entity
import se.llbit.chunky.resources.SolidColorTexture
import se.llbit.json.Json
import se.llbit.json.JsonValue
import se.llbit.math.Transform
import se.llbit.math.Vector3
import se.llbit.math.Vector4
import se.llbit.math.primitive.Box
import se.llbit.math.primitive.Primitive

/**
 * @author Maximilian Stiede
 */
class MaskingEntity(
    var type: MaskType,
    position: Vector3,
    val scale: Vector3,
    val rotation: Vector3, // pitch, yaw, roll
    color: Vector4,
) : Entity(position) {
    enum class MaskType {
        CUBOID,
        PLANE
    }

    val texture = object : SolidColorTexture(color) {
        val colorFloatArray = floatArrayOf(
            color.x.toFloat(),
            color.y.toFloat(),
            color.z.toFloat(),
            color.w.toFloat()
        )

        override fun getColor(u: Double, v: Double): FloatArray = colorFloatArray
    }

    override fun primitives(offset: Vector3): Collection<Primitive> {
        val c = mutableListOf<Primitive>()

        Box(
            -scale.x / 2,
            scale.x / 2,
            -scale.y / 2,
            scale.y / 2,
            -scale.z / 2,
            scale.z / 2
        ).apply {
            transform(
                Transform.NONE
                    .rotateX(rotation.x)
                    .rotateY(rotation.y)
                    .rotateZ(rotation.z)
                    .translate(
                        position.x + offset.x,
                        position.y + offset.y,
                        position.z + offset.z
                    )
            )
        }.apply {
            val zero = Vector4()
            addFrontFaces(c, texture, zero)
            addBackFaces(c, texture, zero)
            addTopFaces(c, texture, zero)
            addBottomFaces(c, texture, zero)
            addLeftFaces(c, texture, zero)
            addRightFaces(c, texture, zero)
        }

        return c
    }

    override fun toJson(): JsonValue {
        return Json.NULL
    }
}
