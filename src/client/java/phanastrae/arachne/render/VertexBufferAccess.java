package phanastrae.arachne.render;

import net.minecraft.client.render.BufferBuilder;

import java.nio.ByteBuffer;

public interface VertexBufferAccess {
    void upload(BufferBuilder.DrawParameters drawParameters, ByteBuffer vertexBuffer, ByteBuffer indexBuffer);
}
