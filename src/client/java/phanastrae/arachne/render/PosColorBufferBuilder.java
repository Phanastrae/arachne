package phanastrae.arachne.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import phanastrae.arachne.mixin.client.BufferBuilderAccessor;

public class PosColorBufferBuilder extends BufferBuilder {

    BufferBuilderAccessor bba = (BufferBuilderAccessor)this;

    public PosColorBufferBuilder(int initialCapacity) {
        super(initialCapacity);
    }

    public void accept(Matrix4f mat, Vec3d p1, Vec3d p2, Vec3d p3, byte r, byte g, byte b, byte a) {
        vertex(mat, p1, r, g, b, a);
        vertex(mat, p2, r, g, b, a);
        vertex(mat, p3, r, g, b, a);
    }

    public void vertex(Matrix4f mat, Vec3d p, byte r, byte g, byte b, byte a) {
        Vector4f v = mat.transform(new Vector4f((float)p.x, (float)p.y, (float)p.z, 1.0f));

        this.putFloat(0, v.x);
        this.putFloat(4, v.y);
        this.putFloat(8, v.z);
        this.putByte(12, r);
        this.putByte(13, g);
        this.putByte(14, b);
        this.putByte(15, a);
        bba.setElementOffset(bba.getElementOffset() + 16);
        this.next();
    }
}
