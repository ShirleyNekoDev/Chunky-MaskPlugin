package de.groovybyte.chunky.maskplugin.math

import de.groovybyte.chunky.maskplugin.utils.divAssign
import de.groovybyte.chunky.maskplugin.utils.plusAssign
import se.llbit.chunky.renderer.WorkerState
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.math.Ray
import se.llbit.math.Vector2
import se.llbit.math.Vector4
import se.llbit.util.Registerable

/**
 * @author Maximilian Stiede
 */
interface SuperSampling : Registerable {
    fun sample(
        pixelX: Int,
        pixelY: Int,
        scene: Scene,
        state: WorkerState,
        trace: (Scene, WorkerState) -> Unit
    )

    companion object {
        val NONE = object : SuperSampling {
            override fun getName() = "NONE"
            override fun getId() = "NONE"
            override fun getDescription() = ""
            override fun sample(
                pixelX: Int,
                pixelY: Int,
                scene: Scene,
                state: WorkerState,
                trace: (Scene, WorkerState) -> Unit
            ) {
                val halfWidth = scene.width / (2.0 * scene.height)
                val invHeight = 1.0 / scene.height

                state.ray = Ray()
                scene.camera().calcViewRay(
                    state.ray,
                    -halfWidth + pixelX * invHeight,
                    -.5 + pixelY * invHeight
                )
                state.ray.o.sub(scene.origin)
                trace(scene, state)
            }

        }
        val HALF_GRID_2x2 = PatternSuperSampling(
            "Half Grid 2x2", "HALF_GRID_2x2",
            Vector2(-1.0 / 4.0, -1.0 / 4.0),
            Vector2(1.0 / 4.0, 1.0 / 4.0),
        )
        val GRID_2x2 = PatternSuperSampling(
            "Grid 2x2", "GRID_2x2",
            Vector2(-1.0 / 4.0, -1.0 / 4.0),
            Vector2(1.0 / 4.0, -1.0 / 4.0),
            Vector2(-1.0 / 4.0, 1.0 / 4.0),
            Vector2(1.0 / 4.0, 1.0 / 4.0),
        )
        val ROTATED_GRID_2x2 = PatternSuperSampling(
            "Rotated Grid 2x2", "ROTATED_GRID_2x2",
            Vector2(-3.0 / 8.0, +1.0 / 8.0),
            Vector2(+1.0 / 8.0, +3.0 / 8.0),
            Vector2(-1.0 / 8.0, -3.0 / 8.0),
            Vector2(+3.0 / 8.0, -1.0 / 8.0),
        )
    }

    class PatternSuperSampling(
        private val name: String,
        private val id: String,
        private vararg val offsets: Vector2
    ) : SuperSampling {
        override fun getName() = name
        override fun getId() = id
        override fun getDescription() = ""
        init {
            require(offsets.isNotEmpty())
        }

        override fun sample(
            pixelX: Int,
            pixelY: Int,
            scene: Scene,
            state: WorkerState,
            trace: (Scene, WorkerState) -> Unit
        ) {
            val halfWidth = scene.width / (2.0 * scene.height)
            val invHeight = 1.0 / scene.height

            state.ray = Ray()
            val color = Vector4()

            offsets.forEach { offset ->
                scene.camera().calcViewRay(
                    state.ray,
//                x * 1.0 / scene.width - 0.5,
//                y * 1.0 / scene.height - 0.5
                    -halfWidth + (pixelX + offset.x) * invHeight,
                    -.5 + (pixelY + offset.y) * invHeight
                )
                state.ray.o.sub(scene.origin)
                trace(scene, state)
                color += state.ray.color
            }
            color /= offsets.size.toDouble()
            state.ray.color = color
        }
    }

    class RandomOffsetSuperSampling(
        private val samples: Int
    ) : SuperSampling {
        override fun getName() = "Random Offset"
        override fun getId() = "RANDOM_OFFSET"
        override fun getDescription() = ""
        override fun sample(
            pixelX: Int,
            pixelY: Int,
            scene: Scene,
            state: WorkerState,
            trace: (Scene, WorkerState) -> Unit
        ) {
            val halfWidth = scene.width / (2.0 * scene.height)
            val invHeight = 1.0 / scene.height

            state.ray = Ray()
            val color = Vector4()

            repeat(samples) {
                val offsetX = state.random.nextDouble() - 0.5
                val offsetY = state.random.nextDouble() - 0.5
                scene.camera().calcViewRay(
                    state.ray,
                    -halfWidth + (pixelX + offsetX) * invHeight,
                    -.5 + (pixelY + offsetY) * invHeight
                )
                state.ray.o.sub(scene.origin)
                trace(scene, state)
                color += state.ray.color
            }
            color /= samples.toDouble()
            state.ray.color = color
        }
    }
}
