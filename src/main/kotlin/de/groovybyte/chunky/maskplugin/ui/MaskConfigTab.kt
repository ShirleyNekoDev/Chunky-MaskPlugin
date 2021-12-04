package de.groovybyte.chunky.maskplugin.ui

import de.groovybyte.chunky.maskplugin.SwitchableRenderManager
import de.groovybyte.chunky.maskplugin.utils.averageFlatColor
import de.groovybyte.chunky.maskplugin.utils.getSafe
import de.groovybyte.chunky.maskplugin.utils.set
import de.groovybyte.chunky.maskplugin.utils.toColor
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import javafx.scene.Node
import javafx.scene.paint.Color
import se.llbit.chunky.block.Air
import se.llbit.chunky.chunk.BlockPalette
import se.llbit.chunky.main.Chunky
import se.llbit.chunky.renderer.scene.Scene
import se.llbit.chunky.ui.ChunkyFxController
import se.llbit.chunky.ui.RenderControlsFxController
import se.llbit.chunky.ui.render.RenderControlsTab
import se.llbit.chunky.world.Material
import se.llbit.math.Octree
import se.llbit.math.Vector4
import se.llbit.util.TaskTracker
import tornadofx.*
import kotlin.collections.set

/**
 * @author Maximilian Stiede
 */
class MaskConfigTab(
    val chunky: Chunky,
) : RenderControlsTab, Fragment() {
    override fun update(scene: Scene) {
        refreshMaskPalette(scene.palette)
    }

    override fun getTabTitle(): String = "Masking"

    override fun getTabContent(): Node = root

    val renderManager: SwitchableRenderManager
        get() = chunky.renderController.renderer as SwitchableRenderManager
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

    inner class ColorBinding(colorRef: Vector4) {
        val defaultColor = colorRef.toColor()

        val color = SimpleObjectProperty(colorRef.toColor()).apply {
            onChange { colorRef.set(it!!) }
        }

        fun setToDefault() {
            color.value = defaultColor;
        }

        fun onChange(callback: (Color) -> Unit) {
            color.onChange {
                if (it != null && it != defaultColor) {
                    callback(it)
                }
            }
        }
    }

    val cloudMask: ColorBinding
    val skyMask: ColorBinding
    val waterMask: ColorBinding
    val bvhMask: ColorBinding
    val actorMask: ColorBinding
    val anyMaterialMask: ColorBinding

    val specificMaterialMasks: ObservableMap<String, ColorBinding> =
        FXCollections.observableHashMap()

    private val materialMaskingPanel: MaterialMaskingPanel
    private val locationMaskingPanel: LocationMaskingPanel

    public fun refreshMaskPalette(palette: BlockPalette) {
        with(renderManager.maskRayTracer) {
            val blockPalette = palette.palette
                .filter { material ->
                    material != Air.INSTANCE &&
                        !material.isWater &&
                        !material.isEntity &&
                        !material.isBlockEntity
                }
                .associateBy(Material::name)
            val addedMaterials = blockPalette.keys - specificMaterialMaskColors.keys
            val removedMaterials = specificMaterialMaskColors.keys - blockPalette.keys

            specificMaterialMaskColors.keys.removeAll(removedMaterials)
            specificMaterialMasks.keys.removeAll(removedMaterials)

            addedMaterials.forEach { materialName ->
                val vec4 = blockPalette[materialName]!!.averageFlatColor
                specificMaterialMaskColors[materialName] = vec4
                specificMaterialMasks[materialName] = ColorBinding(vec4)
            }

            materialMaskingPanel.update()
        }
    }

    init {
        with(renderManager.maskRayTracer) {
            cloudMask = ColorBinding(cloudMaskColor)
            skyMask = ColorBinding(skyMaskColor)
            waterMask = ColorBinding(waterMaskColor)
            bvhMask = ColorBinding(bvhMaskColor)
            actorMask = ColorBinding(actorMaskColor)
            anyMaterialMask = ColorBinding(anyMaterialMaskColor)
        }
        materialMaskingPanel = MaterialMaskingPanel(this)
        locationMaskingPanel = LocationMaskingPanel(this)
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
                    renderManager.renderMask(chunkyScene, taskTracker)
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
}
