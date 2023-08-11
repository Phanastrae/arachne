package phanastrae.arachne.mixin.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import phanastrae.arachne.render.ModRenderLayers;
import phanastrae.arachne.render.BufferBuilderStorageAccess;
import phanastrae.arachne.render.PosColorBufferBuilder;
import phanastrae.arachne.render.SolidBufferBuilder;

import java.util.SortedMap;

@Mixin(BufferBuilderStorage.class)
public class BufferBuilderStorageMixin implements BufferBuilderStorageAccess {
    VertexConsumerProvider.Immediate arachne_builders;

    void arachne_setup() {
        SortedMap<RenderLayer, BufferBuilder> sm = (SortedMap<RenderLayer, BufferBuilder>)Util.make(new Object2ObjectLinkedOpenHashMap(), map -> {
            map.put(ModRenderLayers.getPosColorTriangles(), new PosColorBufferBuilder(ModRenderLayers.getPosColorTriangles().getExpectedBufferSize()));
            map.put(ModRenderLayers.getSolid(), new SolidBufferBuilder(ModRenderLayers.getSolid().getExpectedBufferSize()));
        });
        arachne_builders = VertexConsumerProvider.immediate(sm, new BufferBuilder(256));
    }

    @Override
    public VertexConsumerProvider.Immediate arachne_getBuilders() {
        if(arachne_builders == null) {
            arachne_setup();
        }
        return arachne_builders;
    }
}
