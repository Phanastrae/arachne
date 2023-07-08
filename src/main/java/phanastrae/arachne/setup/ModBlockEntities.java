package phanastrae.arachne.setup;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.block.blockentity.SketchingTableBlockEntity;
import phanastrae.arachne.setup.ModBlocks;

public class ModBlockEntities {

    public static final BlockEntityType<SketchingTableBlockEntity> SKETCHING_TABLE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Arachne.id("sketching_table_entity"),
            FabricBlockEntityTypeBuilder.create(SketchingTableBlockEntity::new, ModBlocks.SKETCHING_TABLE).build());

    public static void init() {
    }
}
