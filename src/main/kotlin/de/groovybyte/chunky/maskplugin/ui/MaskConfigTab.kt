package de.groovybyte.chunky.maskplugin.ui

import de.groovybyte.chunky.maskplugin.MaskColorConfiguration
import de.groovybyte.chunky.maskplugin.utils.ColorBinding
import de.groovybyte.chunky.maskplugin.utils.getSafe
import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import javafx.scene.Node
import se.llbit.chunky.main.Chunky
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.chunky.ui.controller.ChunkyFxController
import se.llbit.chunky.ui.controller.RenderControlsFxController
import se.llbit.chunky.ui.render.RenderControlsTab
import se.llbit.math.Octree
import se.llbit.util.TaskTracker
import tornadofx.*

/**
 * @author Maximilian Stiede
 */
class MaskConfigTab(
    val chunky: Chunky,
    private val colorConfig: MaskColorConfiguration,
) : RenderControlsTab, Fragment() {

    val skyMask: ColorBinding
    val sunMask: ColorBinding
    val cloudMask: ColorBinding
    val waterMask: ColorBinding
    val bvhMask: ColorBinding
    val actorMask: ColorBinding
    val anyMaterialMask: ColorBinding

    private val materialMaskingPanel: MaterialMaskingPanel
    private val locationMaskingPanel: LocationMaskingPanel

    val specificMaterialMasks: ObservableMap<String, ColorBinding> =
        FXCollections.observableHashMap()

    init {
        with(colorConfig) {
            skyMask = ColorBinding(skyMaskColor)
            sunMask = ColorBinding(sunMaskColor)
            cloudMask = ColorBinding(cloudMaskColor)
            waterMask = ColorBinding(waterMaskColor)
            bvhMask = ColorBinding(bvhMaskColor)
            actorMask = ColorBinding(actorMaskColor)
            anyMaterialMask = ColorBinding(anyMaterialMaskColor)
        }

        materialMaskingPanel = MaterialMaskingPanel(this)
        locationMaskingPanel = LocationMaskingPanel(this)

        with(colorConfig) {
            registerBlockPaletteChangeListener { removedMaterials, addedMaterials ->
                specificMaterialMasks.keys.removeAll(removedMaterials)
                specificMaterialMasks.putAll(addedMaterials)
                materialMaskingPanel.update()
            }
        }
    }

    override fun update(scene: Scene) {
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

    override val root = vbox(10.0) {
        paddingAll = 10.0

        isFillWidth = false

        button("Disable Water Octree") {
            action {
                javafx.scene.Scene::class.java.getDeclaredField("waterOctree").run {
                    isAccessible = true
                    set(chunky.sceneManager.scene, Octree(Octree.DEFAULT_IMPLEMENTATION, 1))
                }
            }
        }

        button("Render Mask") {
            action {
                runAsync {
                    updateMask()
                }.apply {
                    setOnFailed {
                        throw exception
                    }
                }
            }
        }

        text("Opacity is not supported!")

        add(materialMaskingPanel)
//        add(locationMaskingPanel)
    }

    public fun updateMask() {
        chunkyScene.refresh()
    }
}
