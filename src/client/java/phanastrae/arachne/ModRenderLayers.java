package phanastrae.arachne;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class ModRenderLayers {
    private static final RenderLayer SOLID = RenderLayer.of(
            "arachne_solid",
            VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
            VertexFormat.DrawMode.TRIANGLES,
            0x200000,
            true,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .program(RenderPhase.SOLID_PROGRAM)
                    .texture(RenderPhase.MIPMAP_BLOCK_ATLAS_TEXTURE)
                    .build(true));

    private static final RenderLayer DISK = RenderLayer.of(
            "arachne_disk",
            VertexFormats.POSITION_COLOR_TEXTURE,
            VertexFormat.DrawMode.QUADS,
            0x1000,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(ModShaders.DISK_SHADER)
                    .build(false));

    public static RenderLayer getSolid() {
        return SOLID;
    }
    public static RenderLayer getDisk() {
        return DISK;
    }
}
