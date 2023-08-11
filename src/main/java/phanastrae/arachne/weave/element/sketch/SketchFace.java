package phanastrae.arachne.weave.element.sketch;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.util.ArachneMath;
import phanastrae.arachne.util.CenteredPlane;
import phanastrae.arachne.util.Line;

import java.util.ArrayList;
import java.util.List;

public class SketchFace extends SketchElement {
    public int id = -1;

    public SketchVertex[] vertices;
    public SketchEdge[] edges;
    public boolean doubleSided = false;

    @Nullable
    SketchPhysicsMaterial physicsMaterial;
    public double area;
    public double virtualThickness;
    public double frontExposure;
    public double backExposure;

    public static final int LAYER_COUNT = 4;
    @Nullable
    public SketchRenderMaterial[] renderMaterial;
    public int[] r;
    public int[] g;
    public int[] b;
    public int[] a;
    public float[][] u;
    public float[][] v;

    public SketchFace(List<SketchVertex> vertices) {
        int l = vertices.size();
        SketchVertex[] v = new SketchVertex[l];
        for(int i = 0; i < l; i++) {
            v[i] = vertices.get(i);
        }
        this.vertices = v;
        SketchEdge[] e = new SketchEdge[l];
        for(int i = 0; i < l; i++) {
            SketchVertex start = v[i];
            SketchVertex end = v[(i+1)%l];
            SketchEdge ed = null;
            if(start.children != null) {
                for (SketchEdge edge : start.children) {
                    if ((edge.start == start && edge.end == end) || (edge.start == end && edge.end == start)) {
                        ed = edge;
                        break;
                    }
                }
            }
            e[i] = ed;
        }
        this.edges = e;

        this.renderMaterial = new SketchRenderMaterial[LAYER_COUNT];
        this.r = new int[LAYER_COUNT];
        this.g = new int[LAYER_COUNT];
        this.b = new int[LAYER_COUNT];
        this.a = new int[LAYER_COUNT];
        this.u = new float[LAYER_COUNT][vertices.size()];
        this.v = new float[LAYER_COUNT][vertices.size()];
        for(int i = 0; i < LAYER_COUNT; i++) {
            for(int j = 0; j < vertices.size(); j++) {
                int k = j % 4;
                this.u[i][j] = (k == 2 || k == 3) ? 1 : 0;
                this.v[i][j] = (k == 1 || k == 2) ? 1 : 0;
            }
        }
    }

    @Override
    public boolean add() {
        boolean bl = super.add();
        if(bl) {
            for(SketchEdge e : this.edges) {
                if(e != null) {
                    e.addChild(this);
                }
            }
        }
        return bl;
    }

    @Override
    public boolean remove() {
        boolean bl = super.remove();
        if(bl) {
            for(SketchEdge e : this.edges) {
                if(e != null) {
                    e.removeChild(this);
                }
            }
        }
        return bl;
    }

    public void setDoubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
    }

    public boolean isDoubleSided() {
        return this.doubleSided;
    }

    public double getMass() {
        SketchPhysicsMaterial physicsMaterial = this.getPhysicsMaterial();
        return physicsMaterial == null ? 0 : physicsMaterial.density * this.virtualThickness * this.area;
    }

    @Override
    public Text getTypeName() {
        return Text.of("Face");
    }

    public Vec3d getAvgGlobalPos() {
        int l = this.vertices.length;
        if(l == 0) return null;
        double x = 0;
        double y = 0;
        double z = 0;
        for(SketchVertex v : this.vertices) {
            x += v.gx;
            y += v.gy;
            z += v.gz;
        }
        x /= l;
        y /= l;
        z /= l;
        return new Vec3d(x, y, z);
    }

    public Vec3d getGlobalNormal() {
        Vec3d avg = this.getAvgGlobalPos();
        if(avg == null) return null;
        int l = this.vertices.length;
        Vec3d normal = Vec3d.ZERO;
        for(int i = 0; i < l; i++) {
            Vec3d p1 = this.vertices[i].getLastGlobalPos();
            Vec3d p2 = this.vertices[(i + 1) % l].getLastGlobalPos();
            normal = normal.add(ArachneMath.getNormal(p1, p2, avg));
        }
        return normal.normalize();
    }

    public void swapFacing() {
        SketchVertex[] newVertices = new SketchVertex[this.vertices.length];
        newVertices[0] = vertices[0];
        for(int i = 1; i < vertices.length; i++) {
            int j = vertices.length - i;
            newVertices[i] = vertices[j];
        }
        this.vertices = newVertices;
    }

    public Pair<Vec3d, Double> getRayHit(Line ray) {
        if(this.vertices == null) return null;
        int l = this.vertices.length;
        if(l < 3) return null;


        Pair<Vec3d, Double> out = null;
        Vec3d p3 = this.getAvgGlobalPos();
        for(int i = 0; i < l; i++) {
            Vec3d p1 = this.vertices[i].getLastGlobalPos();
            Vec3d p2 = this.vertices[(i + 1)%l].getLastGlobalPos();
            Pair<Vec3d, Double> p = getTriangleHit(ray, p1, p2, p3);
            if(p != null) {
                if(out == null) {
                    out = p;
                } else {
                    if(p.getRight() < out.getRight()) {
                        out = p;
                    }
                }
            }
        }
        return out;
    }

    public static Pair<Vec3d, Double> getTriangleHit(Line ray, Vec3d p1, Vec3d p2, Vec3d p3) {
        Vec3d normal = ArachneMath.getNormal(p1, p3, p2);
        CenteredPlane plane = new CenteredPlane(p1, normal);
        Vec3d hit = plane.intersectLine(ray, 0);
        if(hit == null) {
            return null;
        }
        if(hit.lengthSquared() <= 1/64f * 1/64f) {
            return null;
        }
        // TODO: do this more efficiently
        Vec3d line2to1 = p1.subtract(p2);
        Vec3d cross12 = normal.crossProduct(line2to1);
        Vec3d hitFrom1 = hit.subtract(p1);
        if(hitFrom1.dotProduct(cross12) < 0) {
            return null;
        }
        Vec3d line3to2 = p2.subtract(p3);
        Vec3d cross23 = normal.crossProduct(line3to2);
        Vec3d hitFrom2 = hit.subtract(p2);
        if(hitFrom2.dotProduct(cross23) < 0) {
            return null;
        }
        Vec3d line1to3 = p3.subtract(p1);
        Vec3d cross31 = normal.crossProduct(line1to3);
        Vec3d hitFrom3 = hit.subtract(p3);
        if(hitFrom3.dotProduct(cross31) < 0) {
            return null;
        }

        return new Pair<>(hit, hit.subtract(ray.point).length());
    }

    public @Nullable SketchPhysicsMaterial getPhysicsMaterial() {
        return this.physicsMaterial == null ? null : this.physicsMaterial.added ? this.physicsMaterial : null;
    }

    public void setPhysicsMaterial(@Nullable SketchPhysicsMaterial mat) {
        this.physicsMaterial = mat;
    }

    public SketchRenderMaterial getRenderMaterial(int i) {
        if(i < 0 || i >= LAYER_COUNT) return null;
        return this.renderMaterial[i];
    }

    public void setRenderMaterial(SketchRenderMaterial renderMaterial, int i) {
        if(i < 0 || i >= LAYER_COUNT) return;
        this.renderMaterial[i] = renderMaterial;
    }

    public int getR(int i) {
        if(i < 0 || i >= LAYER_COUNT) return 255;
        return this.r[i];
    }

    public int getG(int i) {
        if(i < 0 || i >= LAYER_COUNT) return 255;
        return this.g[i];
    }

    public int getB(int i) {
        if(i < 0 || i >= LAYER_COUNT) return 255;
        return this.b[i];
    }

    public int getA(int i) {
        if(i < 0 || i >= LAYER_COUNT) return 255;
        return this.a[i];
    }

    public void setR(int r, int i) {
        if(i < 0 || i >= LAYER_COUNT) return;
        this.r[i] = r;
    }

    public void setG(int g, int i) {
        if(i < 0 || i >= LAYER_COUNT) return;
        this.g[i] = g;
    }

    public void setB(int b, int i) {
        if(i < 0 || i >= LAYER_COUNT) return;
        this.b[i] = b;
    }

    public void setA(int a, int i) {
        if(i < 0 || i >= LAYER_COUNT) return;
        this.a[i] = a;
    }

    public double getArea() {
        return this.area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getCurrentActualArea() {
        double area = 0;
        int l = this.vertices.length;
        Vec3d p3 = getAvgGlobalPos();
        for(int i = 0; i < l; i++) {
            int j = (i+1)%l;
            Vec3d p1 = this.vertices[i].getLastGlobalPos();
            Vec3d p2 = this.vertices[j].getLastGlobalPos();
            area += getArea(p1, p2, p3);
        }
        return area;
    }

    public double getArea(Vec3d p1, Vec3d p2, Vec3d p3) {
        Vec3d l1 = p1.subtract(p2);
        Vec3d l2 = p1.subtract(p3);
        return 0.5 * l1.crossProduct(l2).length();
    }
}
