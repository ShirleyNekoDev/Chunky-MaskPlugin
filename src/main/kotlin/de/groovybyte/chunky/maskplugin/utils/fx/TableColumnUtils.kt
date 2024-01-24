package de.groovybyte.chunky.maskplugin.utils.fx

import javafx.beans.value.ObservableValue
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn

fun <S, T> tableColumnCC(
    title: String,
    converter: (TableColumn.CellDataFeatures<S, T>) -> ObservableValue<T>,
    cellConstructor: (TableColumn<S, T>) -> TableCell<S, T>
): TableColumn<S, T> {
    return TableColumn<S, T>(title).apply {
        setCellValueFactory(converter)
        setCellFactory(cellConstructor)
    }
}

fun <S, T> tableColumn(
    title: String,
    converter: (S) -> ObservableValue<T>,
    cellInit: TableCell<S, T>.() -> Unit = {},
    cellSet: (TableCell<S, T>).(T) -> Unit = { text = it.toString() }
): TableColumn<S, T> = tableColumnCC(
    title,
    { converter(it.value) }
) {
    object : GenericTableCell<S, T>() {
        init {
            cellInit()
        }

        override fun updateItem(item: T) = cellSet(item)
    }
}

abstract class GenericTableCell<S, T> : TableCell<S, T>() {
    abstract fun updateItem(item: T)
    open fun resetItem(item: T?, empty: Boolean) {
        text = null
        graphic = null
    }

    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            resetItem(item, empty)
        } else {
            updateItem(item)
        }
    }
}

//fun <S, T> readonlyTableColumn(
//    title: String,
//    converter: (S) -> T,
//    cellConstructor: (TableColumn<S, T>) -> TableCell<S, T>
//): TableColumn<S, T> = tableColumn2(
//    title,
//    {
//        ReadOnlyObjectWrapper(converter(it.value))
//    },
//    cellConstructor
//)
