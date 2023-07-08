package phanastrae.arachne.util;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class Line {
    public Vec3d point;
    public Vec3d offset;
    public Line(Vec3d point, Vec3d offset) {
        this.point = point;
        this.offset = offset.normalize();
    }

    // returns the closest point ON THIS LINE to the other line
    @Nullable
    public Vec3d findNearestPointToLine(Line target) {
        // line(s) is this line, line(t) is the target line
        // line(s) = point[a] + s * offset[a] =: p + sa
        // line(t) = point[b] + t * offset[b] =: q + tb
        // want to minimise distance(s, t) <=> minimise distance(s, t)^2 = |line(s) - line(t)|^2
        // = |p-q+sa-tb|^2 = |r+sa-tb|^2    (define r = p - q)
        // = |r|^2 + 2r.(sa-tb) + |sa-tb|^2
        // = |r|^2 + 2sa.r - 2tb.r + (sa-tb).(sa-tb)
        // = |r|^2 + 2sa.r - 2tb.r + s^2|a|^2 + t^2|b|^2 - 2sta.b
        // = |r|^2 + s(2a.r) + t(-2b.r) + st(-2a.b) + s^2(|a|^2) + t^2(|b|^2)
        // d/ds (distance(s,t)^2) = (2a.r) + t(-2a.b) + 2s(|a|^2) = a.(2r) + a.(-2bt) + a.(2sa)) = 2a.(r - bt + as))
        // d/dt (distance(s,t)^2) = (-2b.r) + s(-2a.b) + 2t(|b|^2) = b.(-2r) + b.(-2as) + b.(2tb) = -2b.(r - bt + as)
        // continuous function, so minimum requires that d/ds = 0 and d/dt = 0
        // => a.(r-bt+as) = 0 and b.(r-bt+as) = 0
        // => s(a.a) + t(-a.b) = -a.r and s(a.b) + t(-b.b) = -b.r
        // => [a.a   -a.b][s] = [-a.r]
        //    [a.b   -b.b][t]   [-b.r]
        // (invert matrix)
        // => [s] = 1/(a.a * -b.b - a.b * -a.b) [-b.b   a.b][-a.r]
        //    [t]                               [-a.b   a.a][-b.r]
        // => [s] = 1/(|a|^2|b|^2 - a.b^2) [b.b   -a.b][a.r]
        //    [t]                          [a.b   -a.a][b.r]
        // => s = (b.b * a.r - a.b * b.r) / (|a|^2|b|^2 - a.b^2)
        //    t = (a.b * a.r - a.a * b.r) / (|a|^2|b|^2 - a.b^2)
        // TODO: tidy math if possible
        double aa = this.offset.dotProduct(this.offset);
        double ab = this.offset.dotProduct(target.offset);
        double bb = target.offset.dotProduct(target.offset);
        Vec3d r = this.point.subtract(target.point);
        double ar = this.offset.dotProduct(r);
        double br = target.offset.dotProduct(r);
        double div = aa*bb-ab*ab;
        if(Math.abs(div / (this.offset.lengthSquared() * target.offset.lengthSquared())) < 1E-5) {
            return null;
        }
        double s = (bb*ar-ab*br)/div;
        //double t = (ab*ar-aa*br)/div;

        // return p + sa
        return this.point.add(this.offset.multiply(-s)); // TODO: work out why it's -s and not s
    }
}
