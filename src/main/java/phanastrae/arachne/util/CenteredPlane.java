package phanastrae.arachne.util;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class CenteredPlane {
    public Vec3d center;
    public Vec3d normal;
    public CenteredPlane(Vec3d center, Vec3d normal) {
        this.center = center;
        this.normal = normal.normalize();
    }

    @Nullable
    public Vec3d intersectLine(Line line, double minDistance) {
        // line(t) = point + t * offset
        // want to find t such that: (line(t) - center) dot normal = 0
        // <=> (point - center + t * offset) dot normal = 0
        // <=> t * offset dot normal + (point - center) dot normal = 0
        // <=> t * offset dot normal = (center - point) dot normal
        // <=> t = (center - point) dot normal / offset dot normal

        Vec3d cmp = center.subtract(line.point);
        double cmpdn = cmp.dotProduct(normal);
        double odn = line.offset.dotProduct(normal);
        if(Math.abs(odn) < 1.0E-5) { // line is parallel to the plane
            return null;
        }
        double t = cmpdn / odn;
        if(t < minDistance) {
            t = minDistance;
        }
        return line.point.add(line.offset.multiply(t));
    }
}
