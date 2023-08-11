package phanastrae.arachne.weave.element.sketch;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.util.Line;

import java.util.ArrayList;
import java.util.List;

public class SketchEdge extends SketchElement {
    public int id = -1;

    public SketchVertex start;
    public SketchVertex end;

    public double length;

    @Nullable
    SketchPhysicsMaterial physicsMaterial;
    public double virtualRadius;
    public boolean pullOnly;

    @Nullable
    public SketchRenderMaterial renderMaterial;
    public double thickness;

    ArrayList<SketchFace> children;

    public SketchEdge(SketchVertex node1, SketchVertex node2) {
        this.start = node1;
        this.end = node2;
    }

    public void addChild(SketchFace element) {
        if(this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(element);
    }

    public void removeChild(SketchFace element) {
        if(this.children == null) return;
        this.children.remove(element);
        if(this.children.isEmpty()) {
            this.children = null;
        }
    }

    public List<SketchFace> getChildren() {
        return this.children;
    }


    @Override
    public boolean add() {
        boolean bl = super.add();
        if(bl) {
            this.start.addChild(this);
            this.end.addChild(this);
        }
        return bl;
    }

    @Override
    public boolean remove() {
        boolean bl = super.remove();
        if(bl) {
            this.start.removeChild(this);
            this.end.removeChild(this);
        }
        return bl;
    }

    public double getMass() {
        SketchPhysicsMaterial physicsMaterial = this.getPhysicsMaterial();
        return physicsMaterial == null ? 0 : physicsMaterial.density * this.length * this.virtualRadius * this.virtualRadius * Math.PI;
    }

    @Override
    public Text getTypeName() {
        return Text.of("Edge");
    }

    public Pair<Vec3d, Double> getRayHit(Line ray) {
        // take positions relative to ray origin
        Vec3d lStart = this.start.getLastGlobalPos().subtract(ray.point);
        Vec3d lEnd = this.end.getLastGlobalPos().subtract(ray.point);
        Vec3d lDir = lEnd.subtract(lStart);
        Vec3d offset = ray.offset;
        // try to intersect the ray with a cylinder of radius D around the line

        // ray(t) = t * offset =: to
        // line(t) = lStart + s * lDir = l + sd
        // want to impose that the vector (l+sd-to) is perpendicular to the vector d
        // => (l+sd-to).d = 0
        // => l.d + sd.d - to.d = 0
        // => s(d.d) = to.d - l.d
        // => s = (to.d - l.d)/(d.d)
        // |d| = current line length != 0 so this is always valid (unless line length IS zero, in which case we just get no intersection)
        // => l + sd - to = l + [(to.d - l.d)/(d.d)]d - to = l + t(o.d/d.d)d - (l.d/d.d)d - to
        // = t[(o.d/d.d)d - o] + [l - (l.d/d.d)d]
        // = tA + B
        // where A and B are both vectors

        // want to find the values of t that satisfy |l+sd-to|^2 = D^2 for some specified D and some value of s
        // => D^2 = |l+sd-to|^2 = (l+sd-to).(l+sd-to)
        // so D^2 = (tA+B).(tA+B) = A.At^2 + 2A.Bt + B.B
        // => A.At^2 + 2A.Bt + B.B - D^2 = 0
        // want to pick minimal t so choose negative root
        // => t = [-2A.B - sqrt(4(A.B)^2 - 4A.A(B.B-D^2))]/2A.A
        // => t = [-A.B - sqrt((A.B)^2 - A.A(B.B-D^2))]/A.A
        // with A = [(o.d/d.d)d - o]
        // and  B = [l - (l.d/d.d)d]
        // or t = [a.b - sqrt((a.b)^2 - a.a(b.b-D^2))]/a.a
        // with instead a = [(o.d/d.d)d - o]
        // and  instead b = [(l.d/d.d)d - l]
        // so a.a = [(o.d/d.d)d - o].[(o.d/d.d)d - o] = (o.d/d.d)d.(o.d/d.d)d - 2o.(o.d/d.d)d + o.o
        // = (o.d)(o.d)/(d.d) - 2(o.d)(o.d)/(d.d) + o.o
        // = o.o - (o.d)(o.d)/d.d
        // and b.b = [(l.d/d.d)d - l].[(l.d/d.d)d - l] = (l.d/d.d)d.(l.d/d.d)d - 2l.(l.d/d.d)d + l.l
        // = (l.d)(l.d)/d.d - 2(l.d)(l.d)/d.d + l.l
        // = l.l - (l.d)(l.d)/d.d
        // and a.b = [(o.d/d.d)d - o].[(l.d/d.d)d - l] = (o.d/d.d)d.(l.d/d.d)d - o.(l.d/d.d)d - (o.d/d.d)d.l + o.l
        // = (o.d)(l.d)/d.d - (o.d)(l.d)/d.d - (o.d)(l.d)/d.d + o.l
        // = o.l - (o.d)(l.d)/d.d

        double LINE_RADIUS = 1/128.; // should be smaller than node radius

        double ddotd = lDir.lengthSquared();
        if(ddotd < 1E-5) return null; // line length very short

        double ldotl = lStart.lengthSquared();
        double odoto = offset.lengthSquared();
        double odotd = offset.dotProduct(lDir);
        double odotl = offset.dotProduct(lStart);
        double ldotd = lStart.dotProduct(lDir);
        double adota = odoto - odotd*odotd/ddotd;
        double bdotb = ldotl - ldotd*ldotd/ddotd;
        double adotb = odotl - odotd*ldotd/ddotd;
        double SQRT_INTERIOR = adotb*adotb-adota*(bdotb-LINE_RADIUS*LINE_RADIUS);
        if(SQRT_INTERIOR < 0) return null; // ray does not intersect cylinder

        if(adota < 1E-5) return null; // lines are parallel

        double t = (adotb - Math.sqrt(SQRT_INTERIOR))/adota;
        if(t < 0) return null; // line is behind ray

        double s = (t*odotd - ldotd)/ddotd;
        if(s < 0 || 1 < s) return null; // NOT ON LINE

        Vec3d hit = ray.point.add(ray.offset.multiply(t));
        return new Pair<>(hit, t);
    }

    public double getLength() {
        return this.length;
    }

    public double getCurrentActualLength() {
        return this.start.getPos().subtract(this.end.getPos()).length();
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getVirtualRadius() {
        return this.virtualRadius;
    }

    public void setVirtualRadius(double virtualRadius) {
        this.virtualRadius = virtualRadius;
    }

    public boolean getPullOnly() {
        return this.pullOnly;
    }

    public void setPullOnly(boolean pullOnly) {
        this.pullOnly = pullOnly;
    }

    public @Nullable SketchPhysicsMaterial getPhysicsMaterial() {
        return this.physicsMaterial == null ? null : this.physicsMaterial.added ? this.physicsMaterial : null;
    }

    public void setPhysicsMaterial(@Nullable SketchPhysicsMaterial mat) {
        this.physicsMaterial = mat;
    }
}
