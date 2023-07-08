package phanastrae.arachne.setup;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.entity.WeaveEntity;

public class ModEntities {

    public static final EntityType<WeaveEntity> WEAVE_ENTITY = Registry.register(
        Registries.ENTITY_TYPE,
            Arachne.id("weave_entity"),
        FabricEntityTypeBuilder
                .<WeaveEntity>create()
                .entityFactory(WeaveEntity::new)
                .dimensions(EntityDimensions.fixed(1f, 1f))
                .build()
    ); // TODO modify stuff here

    public static void init() {
        // register any livingEntity attributes
    }
}
