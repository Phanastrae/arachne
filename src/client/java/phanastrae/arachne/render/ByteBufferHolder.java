package phanastrae.arachne.render;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class ByteBufferHolder extends BufferHolder<ByteBuffer> {

    long lastReload = Long.MIN_VALUE;

    boolean released = false;
    final ByteBuffer byteBuffer;
    public ByteBufferHolder(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.lastAccess = System.nanoTime();
    }

    @Override
    public ByteBuffer getBuffer() {
        this.lastAccess = System.nanoTime();
        return this.byteBuffer;
    }

    @Override
    public void release() {
        if(this.released) {
            return;
        }

        MemoryUtil.memFree(byteBuffer);
        this.released = true;
    }

    public void setLastReload(long lastReload) {
        this.lastReload = lastReload;
    }

    public boolean needsReload(long lastReload) {
        return this.lastReload != lastReload;
    }

    public boolean isReleased() {
        return this.released;
    }
}
