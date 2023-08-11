package phanastrae.arachne.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Vector4f;
import phanastrae.arachne.mixin.client.BufferBuilderAccessor;

public class SolidBufferBuilder extends BufferBuilder {

    BufferBuilderAccessor bba = (BufferBuilderAccessor)this;
    BufferBuilderAccess bba2 = (BufferBuilderAccess)this;

    public SolidBufferBuilder(int initialCapacity) {
        super(initialCapacity);
    }

    public void accept(Vector4f v1, Vector4f v2, Vector4f v3, byte r, byte g, byte b, byte a, float[] uvs, int light, float nx, float ny, float nz){
        if(bba.getElementOffset() + 128 > bba.getBuffer().capacity()) {
            bba2.doGrow(128);
        }

        short l1 = (short)(light & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F));
        short l2 = (short)(light >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F));
        byte bnx = BufferVertexConsumer.packByte(nx);
        byte bny = BufferVertexConsumer.packByte(ny);
        byte bnz = BufferVertexConsumer.packByte(nz);
        putVertex(v1.x, v1.y, v1.z, r, g, b, a, uvs[0], uvs[1], l1, l2, bnx, bny, bnz);
        putVertex(v2.x, v2.y, v2.z, r, g, b, a, uvs[2], uvs[3], l1, l2, bnx, bny, bnz);
        putVertex(v3.x, v3.y, v3.z, r, g, b, a, uvs[4], uvs[5], l1, l2, bnx, bny, bnz);
    }

    public void putVertex(float x, float y, float z, byte r, byte g, byte b, byte a, float u, float v, short l1, short l2, byte nx, byte ny, byte nz) {
        this.putFloat(0, x);
        this.putFloat(4, y);
        this.putFloat(8, z);
        this.putByte(12, r);
        this.putByte(13, g);
        this.putByte(14, b);
        this.putByte(15, a);
        this.putFloat(16, u);
        this.putFloat(20, v);
        this.putShort(24, l1);
        this.putShort(26, l2);
        this.putByte(28, nx);
        this.putByte(29, ny);
        this.putByte(30, nz);
        bba.setElementOffset(bba.getElementOffset()+32);
        doNext();
    }

    public void doNext() {
        bba.setVertexCount(bba.getVertexCount()+1);
        //bba2.doGrow(32);
    }
}
