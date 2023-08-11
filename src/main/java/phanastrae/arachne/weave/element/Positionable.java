package phanastrae.arachne.weave.element;

import net.minecraft.util.math.Vec3d;

public interface Positionable {
    double getX();
    double getY();
    double getZ();
    void setX(double d);
    void setY(double d);
    void setZ(double d);

    default void setPos(double x, double y, double z){
        setX(x);
        setY(y);
        setZ(z);
    }

    default void setPos(Vec3d v) {
        setX(v.x);
        setY(v.y);
        setZ(v.z);
    }

    default Vec3d getPos() {
        return new Vec3d(getX(), getY(), getZ());
    }
}
