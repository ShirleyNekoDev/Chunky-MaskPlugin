package de.groovybyte.chunky.maskplugin.ui

import de.groovybyte.chunky.chunkycloudplugin.utils.*
import de.groovybyte.chunky.maskplugin.utils.ColorBinding
import de.groovybyte.chunky.maskplugin.utils.copy
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.TableColumn
import javafx.scene.paint.Color
import tornadofx.*

/**
 * @author Maximilian Stiede
 */
class MaterialMaskingPanel(
    private val maskConfigTab: MaskConfigTab
) : Fragment() {
    fun update() {
        materialMaskTable.sort()
    }

    val innerWidth = 340.0

    val typedMaskTable = tableview<Pair<String, ColorBinding>> {
        readonlyColumn("Type", Pair<String, ColorBinding>::first) {
            cellFormat { name ->
                text = name
                paddingLeft = 6.0
                textFill = Color.WHITE
                style {
                    alignment = Pos.CENTER_LEFT
                }
            }
        }.remainingWidth()
        readonlyColumn("Mask Color", Pair<String, ColorBinding>::second) {
            isSortable = false
            maxWidth = 100.0
            cellFormat { binding ->
                paddingAll = 0.0
                graphic = colorpicker(binding.color) {
                    useMaxWidth = true
                }
            }
        }.contentWidth(useAsMin = true)
        readonlyColumn("Opaque", Pair<String, ColorBinding>::second) {
            isSortable = false
            maxWidth = 70.0
            cellFormat { binding ->
                paddingAll = 0.0
                alignment = Pos.CENTER
                graphic = checkbox {
                    isSelected = binding.color.value.opacity >= 1.0
                    selectedProperty().onChange { checked ->
                        binding.color.apply {
                            value = value.copy(
                                opacity = if (checked) 1.0 else 0.0
                            )
                        }
                        updateMask()
                    }
                }
            }
        }

        with(maskConfigTab) {
            listOf(
                "Sky" to skyMask,
                "Sun" to sunMask,
                "Clouds" to cloudMask,
                "Water" to waterMask,
                "Entities & Special Blocks" to bvhMask,
                "Actors" to actorMask,
                "Unknown" to anyMaterialMask
            ).onEach { (_, colorBinding) ->
                colorBinding.onChange {
                    updateMask()
                }
            }.run(items::addAll)
        }

        prefWidth = innerWidth
        prefHeight = 200.0
        smartResize()
    }

    val materialMaskTable = tableview<Pair<String, ColorBinding>> {
        readonlyColumn("Material", Pair<String, ColorBinding>::first) {
            comparator = String.CASE_INSENSITIVE_ORDER
            sortOrder.add(this)
            sortType = TableColumn.SortType.ASCENDING
            cellFormat { name ->
                text = name
                paddingLeft = 6.0
                textFill = Color.WHITE
                style {
                    alignment = Pos.CENTER_LEFT
                }
            }
        }.remainingWidth()
        readonlyColumn("Mask Color", Pair<String, ColorBinding>::second) {
            isSortable = false
            maxWidth = 100.0
            cellFormat { binding ->
                paddingAll = 0.0
                graphic = colorpicker(binding.color) {
                    useMaxWidth = true
                }
            }
        }.contentWidth(useAsMin = true)

        with(maskConfigTab) {
            items.bind(specificMaterialMasks) { key, value -> key to value }
        }

        prefWidth = innerWidth
        smartResize()
    }

    override val root = titledpane("Material Masking", collapsible = true) {
        isAnimated = false
        isExpanded = false

        vbox(10.0) {
            text("Entities, actors and special blocks like beacons, signs, heads or campfires cannot be masked separately because of technical reasons in the Chunky renderer.") {
                wrappingWidth = innerWidth
            }

            add(typedMaskTable)

            vbox {
                add(materialMaskTable)

                hbox {
                    border(
                        strokeWidth = borderWidth(
                            0, 1, 1, 1
                        )
                    )
                    paddingBottom = 1
                    button("Assign unique material colors") {
                        action {
                            confirmMaterialColorOverwrite(::assignUniqueMaterialColors)
                        }
                    }
                    button("Assign flat material colors") {
                        action {
                            confirmMaterialColorOverwrite(::assignFlatMaterialColors)
                        }
                    }
                    fillWidth()
                }
            }
        }

        expandedProperty().onChangeOnce {
            materialMaskTable.fixColumnWidths()
        }
    }

    init {
        materialMaskTable.items.addListener(ListChangeListener {
            while (it.next()) {
                it.addedSubList.forEach { (_, colorBinding) ->
                    colorBinding.onChange {
                        materialColorsChanged = true
                        updateMask()
                    }
                }
            }
        })
    }

    var materialColorsChanged = false

    private fun confirmMaterialColorOverwrite(action: () -> Unit) {
        if (materialColorsChanged) {
            confirm(
                title = "Replace assigned material colors?",
                header = "Confirmation",
                content = "Do you really want to overwrite your changes to the material colors?",
                confirmButton = ButtonType.YES,
                cancelButton = ButtonType.CANCEL,
                owner = currentWindow,
                actionFn = action
            )
        } else {
            action()
        }
    }

    private fun assignUniqueMaterialColors() {
        haltMaskUpdates = true
        with(maskConfigTab.specificMaterialMasks) {
            val step = 360.0 / size
            values.forEachIndexed { i, colorBinding ->
                colorBinding.color.set(Color.hsb(i * step, 1.0, 1.0))
            }
        }
        haltMaskUpdates = false
        updateMask()
        materialColorsChanged = false
    }

    private fun assignFlatMaterialColors() {
        maskConfigTab.specificMaterialMasks.values
            .forEach(ColorBinding::setToDefault)
        updateMask() // fixes setToDefault not calling onChange
        materialColorsChanged = false
    }

    var haltMaskUpdates = false

    private fun updateMask() {
        if(!haltMaskUpdates)
            maskConfigTab.updateMask()
    }
}
