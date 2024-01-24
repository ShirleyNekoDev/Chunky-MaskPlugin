package de.groovybyte.chunky.maskplugin.ui

import de.groovybyte.chunky.maskplugin.utils.fx.action
import de.groovybyte.chunky.maskplugin.utils.fx.add
import de.groovybyte.chunky.maskplugin.utils.fx.paddingAll
import de.groovybyte.chunky.maskplugin.MaskColorConfiguration
import de.groovybyte.chunky.maskplugin.utils.ColorBinding
import de.groovybyte.chunky.maskplugin.utils.NamedColorBinding
import de.groovybyte.chunky.maskplugin.utils.getSafe
import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import se.llbit.chunky.main.Chunky
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.chunky.ui.controller.ChunkyFxController
import se.llbit.chunky.ui.controller.RenderControlsFxController
import se.llbit.chunky.ui.render.RenderControlsTab
import se.llbit.math.Octree
import se.llbit.util.TaskTracker

/**
 * @author Maximilian Stiede
 */
class MaskConfigTab(
    val chunky: Chunky,
    private val colorConfig: MaskColorConfiguration,
) : RenderControlsTab {

    val skyMask: NamedColorBinding
    val sunMask: NamedColorBinding
    val cloudMask: NamedColorBinding
    val waterMask: NamedColorBinding
    val bvhMask: NamedColorBinding
    val actorMask: NamedColorBinding
    val anyMaterialMask: NamedColorBinding

    private val materialMaskingPanel: MaterialMaskingPanel
//    private val locationMaskingPanel: LocationMaskingPanel

    val specificMaterialMasks: ObservableMap<String, NamedColorBinding> =
        FXCollections.observableHashMap()

    init {
        with(colorConfig) {
            skyMask = NamedColorBinding("Sky", skyMaskColor)
            sunMask = NamedColorBinding("Sun", sunMaskColor)
            cloudMask = NamedColorBinding("Clouds", cloudMaskColor)
            waterMask = NamedColorBinding("Water", waterMaskColor)
            bvhMask = NamedColorBinding("Entities & Special Blocks", bvhMaskColor)
            actorMask = NamedColorBinding("Actors", actorMaskColor)
            anyMaterialMask = NamedColorBinding("Unknown", anyMaterialMaskColor)
        }

        materialMaskingPanel = MaterialMaskingPanel(this)
//        locationMaskingPanel = LocationMaskingPanel(this)

        with(colorConfig) {
            registerBlockPaletteChangeListener { removedMaterials, addedMaterials ->
                println("bpcl: rm=$removedMaterials am=$addedMaterials")
                specificMaterialMasks.keys.removeAll(removedMaterials)
                specificMaterialMasks.putAll(addedMaterials)
                materialMaskingPanel.update()
            }
        }
    }

    override fun update(scene: Scene) {
        println("mct updated block palette $scene.palette.palette")
        colorConfig.updateBlockPalette(scene.palette)
    }

    override fun getTabTitle(): String = "Masking"

    override fun getTabContent(): Node = root

    val chunkyScene: Scene
        get() = chunky.sceneManager.scene

    lateinit var taskTracker: TaskTracker
//    lateinit var entitiesTab: EntitiesTab

    override fun setController(controller: RenderControlsFxController) {
        taskTracker = ChunkyFxController::class.java
            .getDeclaredField("taskTracker")
            .getSafe(controller.chunkyController)
//        entitiesTab = controller.findTabOfType<EntitiesTab>()
    }

    private val root = VBox().apply {
        spacing = 10.0
        paddingAll = 10.0

        isFillWidth = false

        add(Button("Disable Water Octree")) {
            action {
                javafx.scene.Scene::class.java.getDeclaredField("waterOctree").run {
                    isAccessible = true
                    set(chunky.sceneManager.scene, Octree(Octree.DEFAULT_IMPLEMENTATION, 1))
                }
            }
        }

//        Button("Render Mask").apply {
//            action {
//                runAsync {
//                    updateMask()
//                }.apply {
//                    setOnFailed {
//                        throw exception
//                    }
//                }
//            }
//        }

        add(Text("Opacity is not supported!"))

        add(materialMaskingPanel)
//        add(locationMaskingPanel)
    }

    fun updateMask() {
        chunkyScene.refresh()
    }
}
