package de.groovybyte.chunky.maskplugin.ui

import de.groovybyte.chunky.maskplugin.utils.fx.action
import de.groovybyte.chunky.maskplugin.utils.fx.add
import de.groovybyte.chunky.maskplugin.utils.fx.paddingAll
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import se.llbit.chunky.main.Chunky
import se.llbit.chunky.renderer.scene.Camera
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.chunky.ui.render.RenderControlsTab
import se.llbit.math.QuickMath
import se.llbit.math.Ray
import se.llbit.math.Vector3
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Maximilian Stiede
 */
class AnaglyphTab(
    val chunky: Chunky
): RenderControlsTab {
    override fun update(scene: Scene) {
        // none
    }

    override fun getTabTitle(): String = "Anaglyph"

    override fun getTabContent(): Node = root

    val chunkyScene: Scene
        get() = chunky.sceneManager.scene

    val eyeDistance = 0.25 // 0.063

    private val root = VBox().apply {
        spacing = 10.0
        paddingAll = 10.0

        isFillWidth = false

        add(Button("Move Camera Left")) {
            action {
                updateCamera()
            }
        }
        add(Button("Move Camera Right")) {
            action {
                updateCamera()
            }
        }
    }

    fun Camera.rotateAroundPosition(
        rotationCenter: Vector3,
        deltaRad: Double,
    ) {
        val cameraPosition = Vector3().apply {
            set(position)
        }
        println("camera position = " + cameraPosition)
        val distance = Vector3().run {
            sub(rotationCenter, cameraPosition)
            length()
        }
        println("rotation center = " + rotationCenter)

        // apply camera roll
        val dYaw = deltaRad * cos(roll)
        val dPitch = deltaRad * sin(roll)

        val newYaw = (yaw + dYaw) % QuickMath.TAU
        val newPitch = (pitch + dPitch) % QuickMath.TAU
        rotateView(newYaw, newPitch)

        // update internal stuff
//        rotateView(0.0, 0.0)
//        moveUp(0.0)
    }

    fun updateCamera() {
        val camera = chunkyScene.camera()

        val ray = Ray()
        // align ray with camera
        camera.calcViewRay(ray, 0.0, 0.0)
        val cameraPosition = Vector3().apply {
            set(camera.position)
        }
        val cameraDirection = ray.d

        val targetDistance = if(camera.subjectDistance.isFinite()) {
            camera.subjectDistance
        } else {
            val tmpRay = Ray()
            chunkyScene.traceTarget(tmpRay)
            tmpRay.t
        }

        val rotationCenter = Vector3().apply { scaleAdd(targetDistance, cameraDirection, cameraPosition) }

        val angleRad = eyeDistance / targetDistance

        camera.rotateAroundPosition(
            rotationCenter,
            QuickMath.HALF_PI
        )
    }
}
