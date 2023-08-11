package phanastrae.arachne.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

public class ModRenderLayers {

    private static final RenderLayer POS_COLOR_TRIANGLES = RenderLayer.of(
            "arachne_position_color",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.TRIANGLES,
            0x20000,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.COLOR_PROGRAM)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .build(true));

    private static final RenderLayer SOLID = RenderLayer.of(
            "arachne_solid",
            VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
            VertexFormat.DrawMode.TRIANGLES,
            0x20000,
            true,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .program(RenderPhase.TRANSLUCENT_PROGRAM)
                    .texture(RenderPhase.MIPMAP_BLOCK_ATLAS_TEXTURE)
                    .build(true));

    private static final RenderLayer DISK = RenderLayer.of(
            "arachne_disk",
            VertexFormats.POSITION_COLOR_TEXTURE,
            VertexFormat.DrawMode.QUADS,
            0x100,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(ModShaders.DISK_SHADER)
                    .build(false));

    public static RenderLayer getPosColorTriangles() {
        return POS_COLOR_TRIANGLES;
    }

    public static RenderLayer getSolid() {
        return SOLID;
    }
    public static RenderLayer getDisk() {
        return DISK;
    }

    public static VertexConsumerProvider.Immediate getBuffers() {
        return ((BufferBuilderStorageAccess)MinecraftClient.getInstance().getBufferBuilders()).arachne_getBuilders();
    }

    public static final BiFunction<Identifier, Boolean, RenderLayer> SOLID_TEXTURE = Util.memoize((texture, affectsOutline) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                .program(RenderPhase.TRANSLUCENT_PROGRAM)
                .texture(new RenderPhase.Texture(texture, false, false))
                .build(affectsOutline);
        return RenderLayer.of("arachne_solid", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.TRIANGLES, 512, true, true, multiPhaseParameters);
    });
}
