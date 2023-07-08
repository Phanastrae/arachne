package phanastrae.arachne.util;

import net.minecraft.util.math.Vec3d;

public class ArachneMath {
    public static Vec3d lerp(Vec3d v1, Vec3d v2, double t) {
        double xnew = v1.x * (1-t) + v2.x * t;
        double ynew = v1.y * (1-t) + v2.y * t;
        double znew = v1.z * (1-t) + v2.z * t;
        return new Vec3d(xnew, ynew, znew);
    }
}
