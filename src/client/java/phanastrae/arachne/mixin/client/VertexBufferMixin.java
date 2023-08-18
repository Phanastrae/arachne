package phanastrae.arachne.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import phanastrae.arachne.render.VertexBufferAccess;

import java.nio.ByteBuffer;

@Mixin(VertexBuffer.class)
public class VertexBufferMixin implements VertexBufferAccess {
    @Shadow
    public boolean isClosed() {return false;}
    @Shadow
    private VertexFormat uploadVertexBuffer(BufferBuilder.DrawParameters parameters, ByteBuffer vertexBuffer) {return null;}
    @Shadow
    private RenderSystem.ShapeIndexBuffer uploadIndexBuffer(BufferBuilder.DrawParameters parameters, ByteBuffer indexBuffer) {return null;}

    @Override
    public void upload(BufferBuilder.DrawParameters drawParameters, ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
        if (this.isClosed()) {
            return;
        }
        VertexBufferAccessor vba = (VertexBufferAccessor)this;
        RenderSystem.assertOnRenderThread();
        vba.setVertexFormat(this.uploadVertexBuffer(drawParameters, vertexBuffer));
        vba.setSharedSequentialIndexBuffer(this.uploadIndexBuffer(drawParameters, indexBuffer));
        vba.setIndexCount(drawParameters.indexCount());
        vba.setIndexType(drawParameters.indexType());
        vba.setDrawMode(drawParameters.mode());
    }
}
