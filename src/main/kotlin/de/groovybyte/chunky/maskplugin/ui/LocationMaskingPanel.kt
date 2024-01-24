package de.groovybyte.chunky.maskplugin.ui

import de.groovybyte.chunky.maskplugin.MaskingEntity
import de.groovybyte.chunky.maskplugin.utils.fx.action
import de.groovybyte.chunky.maskplugin.utils.fx.add
import javafx.scene.control.Button
import javafx.scene.control.TitledPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import se.llbit.math.ColorUtil
import se.llbit.math.Vector3
import se.llbit.math.Vector4
import kotlin.math.PI
import kotlin.random.Random

/**
 * @author Maximilian Stiede
 */
class LocationMaskingPanel(
    private val maskConfigTab: MaskConfigTab
) : TitledPane("Location Masking", VBox(10.0)) {
    init {
        isCollapsible = true
        isAnimated = false
        isExpanded = false

        (content as Pane).apply {
            add(Text("Manually create masks for spaces in your scene using cuboids or planes."))

            add(Button("add mask entity")) {
                action {
                    spawnEntities()
                }
            }
        }
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
