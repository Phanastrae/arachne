package phanastrae.arachne.weave;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.util.CenteredPlane;
import phanastrae.arachne.util.Line;

import java.util.ArrayList;

public class Face {
    public Node[] nodes;
    public int r = 0xFF;
    public int g = 0xFF;
    public int b = 0xFF;
    public float[] ul;
    public float[] vl;
    @Nullable
    public RenderMaterial renderMaterial = null;

    public Face(ArrayList<Node> nodesAL) { //TODO: add safety for <= 2 nodes if somehow that gets passed in?
        int size = nodesAL.size();
        this.nodes = new Node[size];
        nodesAL.toArray(this.nodes);
        this.ul = new float[size];
        this.vl = new float[size];
        float[] ul2 = new float[]{0, 0, 1, 1};
        float[] vl2 = new float[]{0, 1, 1, 0};
        for(int i = 0; i < size; i++) {
            this.ul[i] = ul2[i % 4];
            this.vl[i] = vl2[i % 4];
        }
    }

    public void setColor(int r, int g, int b) {
        this.r = r & 0xFF;
        this.g = g & 0xFF;
        this.b = b & 0xFF;
    }

    public Vec3d getCenterPos() {
        return this.getCenterPos(1);
    }

    public Vec3d getCenterPos(float tickDelta) {
        // edge cases that should probably never actually happen
        if(this.nodes.length < 3) {
            return null;
        }

        double xAvg = 0;
        double yAvg = 0;
        double zAvg = 0;
        for(Node node : this.nodes) {
            Vec3d pos = node.getPos(tickDelta);
            xAvg += pos.x;
            yAvg += pos.y;
            zAvg += pos.z;
        }
        xAvg /= this.nodes.length;
        yAvg /= this.nodes.length;
        zAvg /= this.nodes.length;
        return new Vec3d(xAvg, yAvg, zAvg);
    }

    public Vec3d rayHit(Line line) {
        Vec3d centerPos = this.getCenterPos();
        if(centerPos == null) {
            return null;
        }
        for(int i = 0; i < this.nodes.length; i++) {
            Node n1 = this.nodes[i];
            Node n2 = this.nodes[(i + 1) % this.nodes.length];
            Vec3d hit = rayHitTriangle(line, centerPos, n1.pos, n2.pos);
            if(hit != null) {
                return hit;
            }
        }
        return null;
    }

    public Vec3d rayHitTriangle(Line line, Vec3d p1, Vec3d p2, Vec3d p3) {
        Vec3d normal = getNormal(p1, p3, p2); // TODO: maybe (if needed) re-order points to make normals and stuff nicer
        CenteredPlane plane = new CenteredPlane(p1, normal);
        Vec3d hit = plane.intersectLine(line, 1/64f);
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

        return hit;
    }

    public Vec3d getNormal() {
        Vec3d normalSum = Vec3d.ZERO;
        Vec3d mid = this.getCenterPos();
        for(int i = 0; i < this.nodes.length; i++) {
            int j = (i + 1) % this.nodes.length;
            normalSum = normalSum.add(getNormal(this.nodes[i].pos, this.nodes[j].pos, mid)); // TODO: check winding order?
        }
        return normalSum.normalize(); // TODO: handle avg = 0?
    }

    public static Vec3d getNormal(Vec3d p1, Vec3d p2, Vec3d p3) {
        Vec3d l1 = p1.subtract(p2);
        Vec3d l2 = p1.subtract(p3);
        return l1.crossProduct(l2).normalize();
    }

    public float getAvgU() {
        float sum = 0;
        for(int i = 0; i < this.ul.length; i++) {
            sum += this.ul[i];
        }
        return sum / this.ul.length;
    }

    public float getAvgV() {
        float sum = 0;
        for(int i = 0; i < this.vl.length; i++) {
            sum += this.vl[i];
        }
        return sum / this.vl.length;
    }
}
