package de.groovybyte.chunky.maskplugin.ui

import de.groovybyte.chunky.maskplugin.utils.ColorBinding
import de.groovybyte.chunky.maskplugin.utils.fx.*
import javafx.collections.ListChangeListener
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text

/**
 * @author Maximilian Stiede
 */
class MaterialMaskingPanel(
    private val maskConfigTab: MaskConfigTab
) : TitledPane("Material Masking", VBox(10.0)) {
    private val innerWidth = 340.0

    private val typedMaskTable = TableView<Pair<String, ColorBinding>>().apply {
//        readonlyColumn("Type", Pair<String, ColorBinding>::first) {
//            cellFormat { name ->
//                text = name
//                paddingLeft = 6.0
//                textFill = Color.WHITE
//                style {
//                    alignment = Pos.CENTER_LEFT
//                }
//            }
//        }.remainingWidth()
//        readonlyColumn("Mask Color", Pair<String, ColorBinding>::second) {
//            isSortable = false
//            maxWidth = 100.0
//            cellFormat { binding ->
//                paddingAll = 0.0
//                graphic = colorpicker(binding.color) {
//                    useMaxWidth = true
//                }
//            }
//        }.contentWidth(useAsMin = true)
//        readonlyColumn("Opaque", Pair<String, ColorBinding>::second) {
//            isSortable = false
//            maxWidth = 70.0
//            cellFormat { binding ->
//                paddingAll = 0.0
//                alignment = Pos.CENTER
//                graphic = checkbox {
//                    isSelected = binding.color.value.opacity >= 1.0
//                    selectedProperty().onChange { checked ->
//                        binding.color.apply {
//                            value = value.copy(
//                                opacity = if (checked) 1.0 else 0.0
//                            )
//                        }
//                        updateMask()
//                    }
//                }
//            }
//        }

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
    }

    private val materialMaskTable = TableView<Pair<String, ColorBinding>>().apply {
//        readonlyColumn("Material", Pair<String, ColorBinding>::first) {
//            comparator = String.CASE_INSENSITIVE_ORDER
//            sortOrder.add(this)
//            sortType = TableColumn.SortType.ASCENDING
//            cellFormat { name ->
//                text = name
//                paddingLeft = 6.0
//                textFill = Color.WHITE
//                style {
//                    alignment = Pos.CENTER_LEFT
//                }
//            }
//        }.remainingWidth()
//        readonlyColumn("Mask Color", Pair<String, ColorBinding>::second) {
//            isSortable = false
//            maxWidth = 100.0
//            cellFormat { binding ->
//                paddingAll = 0.0
//                graphic = colorpicker(binding.color) {
//                    useMaxWidth = true
//                }
//            }
//        }.contentWidth(useAsMin = true)
//
//        with(maskConfigTab) {
//            items.bind(specificMaterialMasks) { key, value -> key to value }
//        }

        prefWidth = innerWidth
    }

    init {
        isCollapsible = true
        isAnimated = false
        isExpanded = false
//        expandedProperty().onChangeOnce {
//            materialMaskTable.fixColumnWidths()
//        }

        (content as Pane).apply {
            add(Text("Entities, actors and special blocks like beacons, signs, heads or campfires cannot be masked separately because of technical reasons in the Chunky renderer.")) {
                wrappingWidth = innerWidth
            }

            add(typedMaskTable)

            add(VBox()) {
                add(materialMaskTable)

                add(HBox()) {
                    paddingBottom = 1.0

                    border(
                        strokeWidth = borderWidth(
                            0, 1, 1, 1
                        )
                    )
                    add(Button("Assign unique material colors")) {
                        action {
                            confirmMaterialColorOverwrite(::assignUniqueMaterialColors)
                        }
                    }
                    add(Button("Assign flat material colors")) {
                        action {
                            confirmMaterialColorOverwrite(::assignFlatMaterialColors)
                        }
                    }
//                    fillWidth()
                }
            }
        }

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

    fun update() {
        materialMaskTable.sort()
    }

    private var materialColorsChanged = false

    private fun confirmMaterialColorOverwrite(action: () -> Unit) {
        if (materialColorsChanged) {
//            confirm(
//                title = "Replace assigned material colors?",
//                header = "Confirmation",
//                content = "Do you really want to overwrite your changes to the material colors?",
//                confirmButton = ButtonType.YES,
//                cancelButton = ButtonType.CANCEL,
//                owner = currentWindow,
//                actionFn = action
//            )
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
