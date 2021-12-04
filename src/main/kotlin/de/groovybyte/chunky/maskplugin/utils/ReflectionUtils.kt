package de.groovybyte.chunky.maskplugin.utils

import java.lang.reflect.Field

/**
 * @author Maximilian Stiede
 */
inline fun <reified T> Field.isOfReadableType(): Boolean =
    T::class.java.isAssignableFrom(type)

inline fun <reified T> Field.isOfWritableType(): Boolean =
    type.isAssignableFrom(T::class.java)

inline fun <reified T> Field.getSafe(obj: Any): T = run {
    isAccessible = true
    get(obj) as T
}

inline fun <reified T> Any.getSafeFromField(fieldName: String) =
    this::class.java.getDeclaredField(fieldName).getSafe<T>(this)

fun <C : Any, T> Class<C>.getSafeFieldGetterFor(fieldName: String): C.() -> T =
    getDeclaredField(fieldName).run {
        isAccessible = true
        {
            @Suppress("UNCHECKED_CAST")
            get(this) as T
        }
    }
