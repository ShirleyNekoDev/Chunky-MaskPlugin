package de.groovybyte.chunky.maskplugin.ui

import de.groovybyte.chunky.maskplugin.EntityPathTracer
import de.groovybyte.chunky.maskplugin.MaskingEntity
import se.llbit.math.ColorUtil
import se.llbit.math.Vector3
import se.llbit.math.Vector4
import tornadofx.*
import kotlin.math.PI
import kotlin.random.Random

/**
 * @author Maximilian Stiede
 */
class LocationMaskingPanel(
    private val maskConfigTab: MaskConfigTab
) : Fragment() {
    override val root = titledpane("Location Masking", collapsible = true) {
        isAnimated = false
        isExpanded = false

        vbox(10.0) {
            text("Manually create masks for spaces in your scene using cuboids or planes.")

            button("add mask entity") {
                action(::spawnEntities)
            }

            button("Mask entities") {
                action {
                    maskEntities()
                }
            }
        }
    }

    fun maskEntities() {
        println("masking...")
        with(maskConfigTab.chunkyScene) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val index = (x + y * width) * 3
                    val color = EntityPathTracer.trace(x, y, this)
//                    if(color.w != 0.0) {
                    sampleBuffer[index + 0] = color.x
                    sampleBuffer[index + 1] = color.y
                    sampleBuffer[index + 2] = color.z
//                    }
                }
            }
//            for (y in 0 until height)
//                for (x in 0 until width)
//                    finalizePixel(x, y)
        }
//        maskConfig.renderManager.canvas.repaint()
    }

    private val random = java.util.Random()
    private fun spawnEntities() {
        val chunkyScene = maskConfigTab.chunkyScene
        for (i in 0..100) {
            val hue = Random.nextDouble()
//            val sat = 1 - sin(Random.nextDouble(0.0, PI))
//            val light = cos(Random.nextDouble(0.0, PI)) / 2 + 0.5
//            println("hue %.2f sat %.2f light %.2f".format(hue, 1.0, 0.5))

            val e = MaskingEntity(
                MaskingEntity.MaskType.CUBOID,
                Vector3(
                    10 * random.nextGaussian(),
                    10 * random.nextGaussian() + (chunkyScene.yClipMin + chunkyScene.yClipMax) / 2,
                    10 * random.nextGaussian()
                ).apply {
                    add(
                        chunkyScene.origin
                    )
                },
                Vector3(
                    Random.nextDouble(1.0, 5.0),
                    Random.nextDouble(1.0, 5.0),
                    Random.nextDouble(1.0, 5.0)
                ),
                Vector3(
                    Random.nextDouble(-PI, PI),
                    Random.nextDouble(-PI, PI),
                    Random.nextDouble(-PI, PI)
                ),
                ColorUtil.RGBfromHSL(
                    hue,
                    1.0,
                    0.5
                ).run {
                    Vector4(
                        x,
                        y,
                        z,
                        1.0 //Random.nextDouble()
                    )
                }
            )
            chunkyScene.entities.add(e)
        }
        chunkyScene.rebuildBvh()
    }
}
