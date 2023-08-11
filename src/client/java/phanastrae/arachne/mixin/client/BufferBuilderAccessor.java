package phanastrae.arachne.mixin.client;

import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    @Accessor
    int getElementOffset();

    @Accessor
    void setElementOffset(int elementOffset);

    @Accessor
    int getVertexCount();

    @Accessor
    void setVertexCount(int vertexCount);

    @Accessor
    ByteBuffer getBuffer();
}
