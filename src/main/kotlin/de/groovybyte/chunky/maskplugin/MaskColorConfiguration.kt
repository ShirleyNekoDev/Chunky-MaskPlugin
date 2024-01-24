package de.groovybyte.chunky.maskplugin

import de.groovybyte.chunky.maskplugin.utils.ColorBinding
import de.groovybyte.chunky.maskplugin.utils.NamedColorBinding
import de.groovybyte.chunky.maskplugin.utils.averageFlatColor
import se.llbit.chunky.block.minecraft.Air
import se.llbit.chunky.chunk.BlockPalette
import se.llbit.chunky.world.Material
import se.llbit.math.Vector4

data class MaskColorConfiguration(
    val skyMaskColor: Vector4 = Vector4(0.0, 0.0, 0.0, 1.0),
    val sunMaskColor: Vector4 = Vector4(0.0, 0.0, 0.0, 1.0),
    val cloudMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val waterMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val bvhMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val actorMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val anyMaterialMaskColor: Vector4 = Vector4(1.0, 1.0, 1.0, 1.0),
    val specificMaterialMaskColors: MutableMap<String, Vector4> = HashMap(),
) {
    fun updateBlockPalette(palette: BlockPalette) {
        val blockPalette = palette.palette
            .filter { material ->
                material != Air.INSTANCE &&
                    !material.isWater &&
                    !material.isEntity &&
                    !material.isBlockEntity
            }
            .associateBy(Material::name)

        val removedMaterials: Set<String> = specificMaterialMaskColors.keys - blockPalette.keys
        specificMaterialMaskColors.keys.removeAll(removedMaterials)

        val addedMaterials = (blockPalette.keys - specificMaterialMaskColors.keys)
            .associateWith { materialName ->
                val vec4 = blockPalette[materialName]!!.averageFlatColor
                specificMaterialMaskColors[materialName] = vec4
                NamedColorBinding(materialName, vec4)
            }

        blockPaletteChangeListeners.forEach {
            it.onChange(removedMaterials, addedMaterials)
        }
    }

    private val blockPaletteChangeListeners: MutableCollection<BlockPaletteChangeListener> =
        ArrayList(1)

    fun registerBlockPaletteChangeListener(
        listener: BlockPaletteChangeListener
    ) = blockPaletteChangeListeners.add(listener)

    fun interface BlockPaletteChangeListener {
        fun onChange(
            removedMaterials: Set<String>,
            addedMaterials: Map<String, NamedColorBinding>
        )
    }
}
