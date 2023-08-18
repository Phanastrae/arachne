package phanastrae.arachne.render;

import net.minecraft.client.gl.VertexBuffer;

public class VertexBufferHolder extends BufferHolder<VertexBuffer> {

    final VertexBuffer vertexBuffer;
    public VertexBufferHolder(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
        this.lastAccess = System.nanoTime();
    }

    @Override
    public VertexBuffer getBuffer() {
        this.lastAccess = System.nanoTime();
        return this.vertexBuffer;
    }

    @Override
    public void release() {
        if(this.vertexBuffer.isClosed()) {
            return;
        }
        this.vertexBuffer.close();
    }
}
