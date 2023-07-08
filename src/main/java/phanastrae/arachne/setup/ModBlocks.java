package phanastrae.arachne.setup;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.Instrument;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.block.MysticLoomBlock;
import phanastrae.arachne.block.SketchingTableBlock;

public class ModBlocks {
    public static final Block SKETCHING_TABLE = new SketchingTableBlock(FabricBlockSettings.create().mapColor(MapColor.SPRUCE_BROWN).instrument(Instrument.BASS).strength(2.5f).sounds(BlockSoundGroup.WOOD).burnable());
    // TODO: tweak ML properties
    public static final Block MYSTIC_LOOM = new MysticLoomBlock(FabricBlockSettings.create().mapColor(MapColor.PURPLE).instrument(Instrument.BASEDRUM).requiresTool().strength(5.0f, 1200.0f).sounds(BlockSoundGroup.AMETHYST_BLOCK).burnable());


    public static void init() {
        register(SKETCHING_TABLE, "sketching_table");
        register(MYSTIC_LOOM, "mystic_loom");
    }

    public static <T extends Block> void register(T block, String name) {
        Registry.register(Registries.BLOCK, Arachne.id(name), block);
    }
}
