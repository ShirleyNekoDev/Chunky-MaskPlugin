package de.groovybyte.chunky.maskplugin.ui

import de.groovybyte.chunky.maskplugin.utils.ColorBinding
import de.groovybyte.chunky.maskplugin.utils.NamedColorBinding
import de.groovybyte.chunky.maskplugin.utils.fx.*
import javafx.beans.binding.Bindings
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.cell.CheckBoxTreeCell
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

    fun newMaskColorColumn() = tableColumnCC<NamedColorBinding, NamedColorBinding>(
        "Mask Color",
        { ReadOnlyObjectWrapper(it.value) },
        {
            object : GenericTableCell<NamedColorBinding, NamedColorBinding>() {
                init {
                    paddingAll = 0.0
                }

                override fun resetItem(item: NamedColorBinding?, empty: Boolean) {
                    (graphic as? NewColorPicker)?.backgroundProperty()?.unbind()
                    graphic = null
                }

                override fun updateItem(item: NamedColorBinding) {
                    graphic = ColorPicker().apply {
                        valueProperty().bindBidirectional(item.color)
                    }
//                    graphic = NewColorPicker(item.color)
                }
            }
        }
    ).apply {
        isSortable = false
        maxWidth = 100.0
    }
    fun newOpacityColumn() = tableColumnCC<NamedColorBinding, NamedColorBinding>(
        "Opaque",
        { ReadOnlyObjectWrapper(it.value) },
        { col ->
            CheckBoxTableCell({ i ->
                Propert
                col.getCellData(i).color.
            })
//            object : GenericTableCell<NamedColorBinding, NamedColorBinding>() {
//                init {
//                    paddingAll = 0.0
//                    alignment = Pos.CENTER
//                }
//
//                override fun resetItem(item: NamedColorBinding?, empty: Boolean) {
//                    (graphic as? NewColorPicker)?.backgroundProperty()?.unbind()
//                    graphic = null
//                }
//
//                override fun updateItem(item: NamedColorBinding) {
//                    graphic = CheckBoxTableCell {
//
//                    }
////                    graphic = checkbox {
////                        isSelected = binding.color.value.opacity >= 1.0
////                        selectedProperty().onChange { checked ->
////                            binding.color.apply {
////                                value = value.copy(
////                                    opacity = if (checked) 1.0 else 0.0
////                                )
////                            }
////                            updateMask()
////                        }
////                    }
//                }
//            }
        }
    ).apply {
        isSortable = false
        maxWidth = 70.0
    }
//    readonlyColumn("Opaque", Pair<String, ColorBinding>::second) {
//            cellFormat { binding ->
//                paddingAll = 0.0
//                alignment = Pos.CENTER
//                graphic =
//            }
//        }

    private val typedMaskTable = TableView<NamedColorBinding>().apply {
        columns.add(tableColumn<NamedColorBinding, String>(
            "Type",
            { it.name.asReadOnlyProperty() },
            {
                paddingLeft = 6.0
                textFill = Color.WHITE
                alignment = Pos.CENTER_LEFT
            },
            {
                text = it
            }
        ))
        columns.add(newMaskColorColumn())
//        columns.add(TableColumn<NamedColorBinding, ColorBinding>("Mask Color").apply {
//            setCellFactory {
//                object : TableCell<NamedColorBinding, ColorBinding>() {
//                    init {
//                        isSortable = false
//                        maxWidth = 100.0
//                        paddingAll = 0.0
//                        maxWidth = Double.MAX_VALUE
//                    }
//
//                    override fun updateItem(item: ColorBinding?, empty: Boolean) {
//                        super.updateItem(item, empty)
//                        if (empty || item == null) {
//                            text = null
//                            graphic = null
//                        } else {
//                            graphic = Button().apply {
//                                item.color.addListener { _, _, n ->
//                                    background = Background(
//                                        BackgroundFill(
//                                            n,
//                                            CornerRadii.EMPTY,
//                                            Insets.EMPTY
//                                        )
//                                    )
//                                }
//                            }
////                            colorpicker(item.color) {
////                                useMaxWidth = true
////                            }
//                        }
//                    }
//                }
//            }
//        })
        columns.add(TableColumn<NamedColorBinding, ColorBinding>("Opaque"))

//        columns.add(readonlyColumn("Type") {
//
//        })

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
                skyMask,
                sunMask,
                cloudMask,
                waterMask,
                bvhMask,
                actorMask,
                anyMaterialMask
            ).onEach { colorBinding ->
                colorBinding.onChange {
                    updateMask()
                }
            }.run(items::addAll)
        }

        prefWidth = innerWidth
        prefHeight = 200.0
    }

    private val materialMaskTable = TableView<NamedColorBinding>().apply {
        columns.add(TableColumn<NamedColorBinding, String>("Material").apply {
            //            setCellFactory {
//                object : TableCell<NamedColorBinding, String>() {
//                    init {
//                        comparator = String.CASE_INSENSITIVE_ORDER
////                        sortOrder.add(this)
//                        sortType = TableColumn.SortType.ASCENDING
//
//                        paddingLeft = 6.0
//                        textFill = Color.WHITE
//                        alignment = Pos.CENTER_LEFT
//                        // remainingWidth()
//                    }
//
//                    override fun updateItem(item: String?, empty: Boolean) {
//                        println("mmt matname ui $item $empty")
//                        super.updateItem(item, empty)
//                        if (empty || item == null) {
//                            text = null
//                            graphic = null
//                        } else {
//                            text = item
//                        }
//                    }
//                }
//            }
        })
        columns.add(newMaskColorColumn())
        columns.add(TableColumn<NamedColorBinding, ColorBinding>("Mask Color").apply {
//            setCellFactory {
//                object : TableCell<NamedColorBinding, ColorBinding>() {
//                    init {
//                        isSortable = false
//                        maxWidth = 100.0
//                        paddingAll = 0.0
//                        maxWidth = Double.MAX_VALUE
//                        // contentWidth(useAsMin = true)
//                    }
//
//                    override fun updateItem(newitem: ColorBinding?, empty: Boolean) {
//                        super.updateItem(newitem, empty)
//                        println("mmt maskcol ui $item $newitem $empty")
//                        if (empty || newitem == null) {
//                            text = null
//                            graphic = null
//                        } else {
//                            graphic = Button().apply {
////                                item.color.addListener { _, _, n ->
//                                background = Background(
//                                    BackgroundFill(
//                                        item.color.value,
//                                        CornerRadii.EMPTY,
//                                        Insets.EMPTY
//                                    )
//                                )
////                                }
//                            }
////                            colorpicker(item.color) {
////                                useMaxWidth = true
////                            }
//                        }
//                    }
//                }
//            }
        })

        items.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasPermutated()) {
                    println("items permutated")
//                    for (i in it.getFrom() until it.getTo()) {
//                        //permutate
//                    }
                } else if (it.wasUpdated()) {
                    println("items updated")
                } else {
                    for (remitem in it.getRemoved()) {
                        println("items - $remitem")
                    }
                    for (additem in it.getAddedSubList()) {
                        println("items + $additem")
                    }
                }
            }
        })

        with(maskConfigTab) {
            specificMaterialMasks.addListener(MapChangeListener { change ->
                println("mapchange k=${change.key} va=${change.valueAdded} vr=${change.valueRemoved}")
                when {
                    change.wasRemoved() -> items.remove(change.valueRemoved)
                    change.wasAdded() -> items.add(change.valueAdded)
                }
            })
        }

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
                it.addedSubList.forEach { colorBinding ->
                    colorBinding.onChange {
                        materialColorsChanged = true
                        updateMask()
                    }
                }
            }
        })
    }

    fun update() {
        println("mmp update()")
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
        if (!haltMaskUpdates)
            maskConfigTab.updateMask()
    }
}
