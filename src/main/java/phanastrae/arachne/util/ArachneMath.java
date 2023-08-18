package phanastrae.arachne.util;

import net.minecraft.util.math.Vec3d;

public class ArachneMath {
    public static Vec3d lerp(Vec3d v1, Vec3d v2, double t) {
        double xnew = v1.x * (1-t) + v2.x * t;
        double ynew = v1.y * (1-t) + v2.y * t;
        double znew = v1.z * (1-t) + v2.z * t;
        return new Vec3d(xnew, ynew, znew);
    }

    public static Vec3d getNormal(Vec3d p1, Vec3d p2, Vec3d p3) {
        Vec3d l1 = p1.subtract(p2);
        Vec3d l2 = p1.subtract(p3);
        return l1.crossProduct(l2).normalize();
    }

    public static void getNormal(float[] p1, float[] p2, float[] p3, float[] fill) {
        double l1x = p1[0]-p2[0];
        double l1y = p1[1]-p2[1];
        double l1z = p1[2]-p2[2];
        double l2x = p1[0]-p3[0];
        double l2y = p1[1]-p3[1];
        double l2z = p1[2]-p3[2];
        double crossx = l1y*l2z-l1z*l2y;
        double crossy = l1z*l2x-l1x*l2z;
        double crossz = l1x*l2y-l1y*l2x;
        double len = crossx*crossx+crossy*crossy+crossz*crossz;
        double normFactor = 1/Math.sqrt(len);
        fill[0] = (float)(crossx * normFactor);
        fill[1] = (float)(crossy * normFactor);
        fill[2] = (float)(crossz * normFactor);
    }
}
