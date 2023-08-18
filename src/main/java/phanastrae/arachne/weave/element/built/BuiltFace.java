package phanastrae.arachne.weave.element.built;

import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.util.math.Vec3d;
import phanastrae.arachne.util.ArachneMath;
import phanastrae.arachne.util.CenteredPlane;
import phanastrae.arachne.util.Line;
import phanastrae.arachne.weave.element.active.ActiveNode;
import phanastrae.arachne.weave.element.active.ForceAdder;
import phanastrae.arachne.weave.WeaveStateUpdater;
import phanastrae.arachne.weave.element.sketch.SketchFace;
import phanastrae.old.Face;
import phanastrae.old.Node;

import java.util.Random;
import java.util.function.Supplier;

public class BuiltFace implements ForceAdder {
    public final int id;

    public final int[] nodes;
    public final double oneByNodeCount;

    public final double area;

    public final double frontExposure;
    public final double backExposure;

    public final boolean doubleSided;

    public BuiltFace(SketchFace face) {
        this.id = face.id;

        int[] nodes = new int[face.vertices.length];
        for(int i = 0; i < face.vertices.length; i++) {
            nodes[i] = face.vertices[i].id;
        }
        this.nodes = nodes;
        this.oneByNodeCount = 1.0 / this.nodes.length;

        this.area = face.area;
        this.frontExposure = face.frontExposure;
        this.backExposure = face.backExposure;

        this.doubleSided = face.doubleSided;
    }

    public ActiveNode getNodeInput(int i, WeaveStateUpdater weaveStateUpdater) {
        return weaveStateUpdater.getNodeInput(this.nodes[i]);
    }

    public ActiveNode getNodeOutput(int i, WeaveStateUpdater weaveStateUpdater) {
        return weaveStateUpdater.getNodeInput(this.nodes[i]);
    }

    @Override
    public void addForces(WeaveStateUpdater weaveStateUpdater) {
    }

    public void applyWind(WeaveStateUpdater weaveStateUpdater, double windx, double windy, double windz, double fluidDensity, int multiplier) {
        int l = this.nodes.length;

        double maxSegmentArea = this.area < 0 ? Double.POSITIVE_INFINITY : this.area / l;

        double p3x = 0;
        double p3y = 0;
        double p3z = 0;
        double vx = 0;
        double vy = 0;
        double vz = 0;
        for (int i = 0; i < l; i++) {
            ActiveNode n = this.getNodeInput(i, weaveStateUpdater);
            p3x += n.x;
            p3y += n.y;
            p3z += n.z;
            vx += n.vx;
            vy += n.vy;
            vz += n.vz;
        }
        p3x *= oneByNodeCount;
        p3y *= oneByNodeCount;
        p3z *= oneByNodeCount;
        vx *= oneByNodeCount;
        vy *= oneByNodeCount;
        vz *= oneByNodeCount;

        double rx = vx-windx;
        double ry = vy-windy;
        double rz = vz-windz;

        double RS = rx*rx+ry*ry+rz*rz;
        if (RS < 1E-7) return;

        for (int i = 0; i < l; i++) {
            int j = (i+1)%l;
            ActiveNode n1 = this.getNodeInput(i, weaveStateUpdater);
            ActiveNode n2 = this.getNodeInput(j, weaveStateUpdater);

            double p1x = n1.x;
            double p1y = n1.y;
            double p1z = n1.z;
            double p2x = n2.x;
            double p2y = n2.y;
            double p2z = n2.z;

            // calculate normal
            double l12x = p1x - p2x;
            double l12y = p1y - p2y;
            double l12z = p1z - p2z;
            double l13x = p1x - p3x;
            double l13y = p1y - p3y;
            double l13z = p1z - p3z;
            Vec3d l12 = new Vec3d(l12x, l12y, l12z);
            Vec3d l13 = new Vec3d(l13x, l13y, l13z);
            double cx = l12y*l13z - l13y*l12z;
            double cy = l12z*l13x - l13z*l12x;
            double cz = l12x*l13y - l13x*l12y;
            double c = 1/Math.sqrt(cx*cx+cy*cy+cz*cz);
            Vec3d lcross = new Vec3d(cx, cy, cz);
            double nx = cx * c;
            double ny = cy * c;
            double nz = cz * c;
            double relVelocityDotNormal = nx*rx + ny*ry + nz*rz;

            // project the points onto a plane with normal = relativeVelocity
            double d12 = (l12.x*rx+l12.y*ry+l12.z*rz)/-RS;
            double d13 = (l13.x*rx+l13.y*ry+l13.z*rz)/-RS;

            //Vec3d q = p.add(relativeVelocity.multiply(d));

            // calculate area of projected triangle
            double q12x = l12x + rx*d12;
            double q12y = l12y + ry*d12;
            double q12z = l12z + rz*d12;
            double q13x = l13x + rx*d13;
            double q13y = l13y + ry*d13;
            double q13z = l13z + rz*d13;
            double cx2 = q13y*q12z - q13z*q12y;
            double cy2 = q13z*q12x - q13x*q12z;
            double cz2 = q13x*q12y - q13y*q12x;
            double c2 = cx2*cx2+cy2*cy2+cz2*cz2;
            double area = Math.sqrt(c2) / 2;
            // limit area primarily for stability
            if(area > maxSegmentArea) {
                area = maxSegmentArea;
            }

            // TODO: stop assuming uniform(ish) wind that flows through everything i.e. non-air-exposed faces should not experience wind

            // TODO: consider changing maths
            // calculate force
            // works well enough but probably isn't 100% accurate
            // F = -2 * D * A * |v-V| * ((v-V).N) * N
            double mag = -2 * fluidDensity * area * Math.sqrt(RS) * relVelocityDotNormal;

            double m = mag * 0.5 * multiplier;

            double Fx = nx * m;
            double Fy = ny * m;
            double Fz = nz * m;

            // apply force
            this.getNodeOutput(i, weaveStateUpdater).addForce(Fx, Fy, Fz);
            this.getNodeOutput(j, weaveStateUpdater).addForce(Fx, Fy, Fz);
        }
    }

    public Vec3d getCenterPos(WeaveStateUpdater weaveStateUpdater) {
        double avgx = 0;
        double avgy = 0;
        double avgz = 0;
        for (int i : this.nodes) {
            ActiveNode node = weaveStateUpdater.getNodeInput(i);
            avgx += node.x;
            avgy += node.y;
            avgz += node.z;
        }
        avgx *= oneByNodeCount;
        avgy *= oneByNodeCount;
        avgz *= oneByNodeCount;
        return new Vec3d(avgx, avgy, avgz);
    }

    public void getCenterPos(WeaveStateUpdater weaveStateUpdater, float[] fill) {
        double avgx = 0;
        double avgy = 0;
        double avgz = 0;
        for (int i : this.nodes) {
            ActiveNode node = weaveStateUpdater.getNodeInput(i);
            avgx += node.x;
            avgy += node.y;
            avgz += node.z;
        }
        avgx *= oneByNodeCount;
        avgy *= oneByNodeCount;
        avgz *= oneByNodeCount;
        fill[0] = (float)avgx;
        fill[1] = (float)avgy;
        fill[2] = (float)avgz;
    }

    public Vec3d getNormal(WeaveStateUpdater weaveStateUpdater) {
        int l = this.nodes.length;
        if(l < 3) return Vec3d.ZERO;

        Vec3d total = Vec3d.ZERO;
        Vec3d p3 = getCenterPos(weaveStateUpdater);
        for(int i = 0; i < l; i++) {
            int j = (i + 1)%l;
            Vec3d p1 = getNodeInput(i, weaveStateUpdater).getPosition();
            Vec3d p2 = getNodeInput(j, weaveStateUpdater).getPosition();
            total = total.add(ArachneMath.getNormal(p1, p2, p3));
        }
        return total.normalize();
    }

    public void getNormal(WeaveStateUpdater weaveStateUpdater, float[] fill, float[] center) {
        int l = this.nodes.length;
        if(l < 3) {
            fill[0] = 0;
            fill[1] = 0;
            fill[2] = 0;
        }

        float totx = 0;
        float toty = 0;
        float totz = 0;
        float[] p1 = new float[3];
        float[] p2 = new float[3];
        float[] addition = new float[3];
        for(int i = 0; i < l; i++) {
            int j = (i + 1)%l;
            getNodeInput(i, weaveStateUpdater).getPosition(p1);
            getNodeInput(j, weaveStateUpdater).getPosition(p2);
            ArachneMath.getNormal(p1, p2, center, addition);
            totx += addition[0];
            toty += addition[1];
            totz += addition[2];
        }
        double norm = 1/Math.sqrt(totx*totx+toty*toty+totz*totz);
        fill[0] = (float)(totx*norm);
        fill[1] = (float)(toty*norm);
        fill[2] = (float)(totz*norm);
    }
}
