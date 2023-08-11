package phanastrae.arachne.weave.element.sketch;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.util.Line;
import phanastrae.arachne.weave.element.Positionable;

import java.util.ArrayList;
import java.util.List;

public class SketchVertex extends SketchElement implements RayTarget, Positionable {
    public int id = -1;

    // local pos
    public double x;
    public double y;
    public double z;
    // last global pos
    public float gx;
    public float gy;
    public float gz;

    public boolean isStatic = false;
    ArrayList<SketchEdge> children;

    public Vec3d getLastGlobalPos() {
        return new Vec3d(gx, gy, gz);
    }

    @Nullable
    SketchPhysicsMaterial physicsMaterial;
    public double virtualVolume;

    @Nullable
    public SketchRenderMaterial renderMaterial;
    public double size;

    public SketchVertex(SketchVertexCollection parent) {
        this.parent = parent;
    }

    public void addChild(SketchEdge element) {
        if(this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(element);
    }

    public void removeChild(SketchEdge element) {
        if(this.children == null) return;
        this.children.remove(element);
        if(this.children.isEmpty()) {
            this.children = null;
        }
    }

    public List<SketchEdge> getChildren() {
        return this.children;
    }

    @Override
    public Text getTypeName() {
        return Text.translatable("arachne.editor.type.vertex");
    }

    @Nullable
    @Override
    public Pair<Vec3d, Double> getRayHit(Line ray) {
        double NODE_RADIUS = 1/64.;
        Vec3d nodePos = this.getLastGlobalPos();
        Vec3d rayPos = ray.point.subtract(nodePos);
        Vec3d rayOffset = ray.offset;
        // ray(t) = pos + t * offset
        // want to find t s.t. x^2 + y^2 + z^2 = NODE_RADIUS^2
        // i.e. |ray(t)|^2 = NODE_RADIUS^2
        // |ray(t)|^2 = ray(t).ray(t) = (pos + t * offset).(pos + t * offset)
        // = pos.pos + 2t*pos.offset + t^2 * offset.offset
        // => t^2(offset.offset) + t(2*pos.offset) + (pos.pos-NODE_RADIUS^2) =0
        // so t = [-2(pos.offset) +- sqrt(4t^2(pos.offset)^2-4*(pos.pos-NODE_RADIUS^2)*(offset.offset))]/2*(offset.offset)
        // => t = [-(pos.offset) +- sqrt((pos.offset)^2-(pos.pos-NODE_RADIUS^2)*(offset.offset))]/(offset.offset)
        // want minimum distance to choose - sqrt
        double pdo = rayPos.dotProduct(rayOffset);
        double pdpminnr = rayPos.dotProduct(rayPos)-NODE_RADIUS*NODE_RADIUS;
        double odo = rayOffset.dotProduct(rayOffset);
        double SQRT_INTERIOR = pdo*pdo - pdpminnr*odo;
        if(SQRT_INTERIOR < 0) return null; // line and node do not intersect

        double t = (-pdo - Math.sqrt(SQRT_INTERIOR))/odo;
        if(t < 0) {
            return null;
        } else {
            return new Pair<Vec3d, Double>(ray.point.add(rayOffset.multiply(t)), t);
        }
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public void setX(double d) {
        this.x = d;
    }

    @Override
    public void setY(double d) {
        this.y = d;
    }

    @Override
    public void setZ(double d) {
        this.z = d;
    }

    @Nullable
    public SketchEdge getConnection(SketchVertex v) {
        if(this.children == null) return null;

        for(SketchElement e : this.children) {
            if(e instanceof SketchEdge edge) {
                if(edge.start == v || edge.end == v) {
                    return edge;
                }
            }
        }
        return null;
    }

    public double getVirtualVolume() {
        return this.virtualVolume;
    }

    public void setVirtualVolume(double virtualVolume) {
        this.virtualVolume = virtualVolume;
    }

    public boolean getIsStatic() {
        return this.isStatic;
    }

    public void setIsStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public @Nullable SketchPhysicsMaterial getPhysicsMaterial() {
        return this.physicsMaterial == null ? null : this.physicsMaterial.added ? this.physicsMaterial : null;
    }

    public void setPhysicsMaterial(@Nullable SketchPhysicsMaterial mat) {
        this.physicsMaterial = mat;
    }
}
