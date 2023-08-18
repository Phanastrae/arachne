package phanastrae.arachne.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VertexBuffer.class)
public interface VertexBufferAccessor {
    @Accessor
    int getVertexBufferId();
    @Accessor
    void setVertexFormat(VertexFormat vertexFormat);
    @Accessor
    void setSharedSequentialIndexBuffer(RenderSystem.ShapeIndexBuffer sharedSequentialIndexBuffer);
    @Accessor
    void setIndexType(VertexFormat.IndexType indexType);
    @Accessor
    void setIndexCount(int indexCount);
    @Accessor
    void setDrawMode(VertexFormat.DrawMode drawMode);
}
