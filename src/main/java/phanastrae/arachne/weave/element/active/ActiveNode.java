package phanastrae.arachne.weave.element.active;

import net.minecraft.util.math.Vec3d;
import phanastrae.arachne.weave.element.built.BuiltNode;

public class ActiveNode implements ForceAcceptor {

    public BuiltNode builtNode;

    // pos
    public double x;
    public double y;
    public double z;
    // velocity
    public double vx;
    public double vy;
    public double vz;
    // acceleration
    public double ax;
    public double ay;
    public double az;
    // force
    public double fx;
    public double fy;
    public double fz;

    public ActiveNode(BuiltNode builtNode) {
        this.builtNode = builtNode;
        this.x = builtNode.x;
        this.y = builtNode.y;
        this.z = builtNode.z;
    }

    public void addPosition(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void addVelocity(double x, double y, double z) {
        this.vx += x;
        this.vy += y;
        this.vz += z;
    }

    public void addPosition(double x, double y, double z, ActiveNode from) {
        this.x = x + from.x;
        this.y = y + from.y;
        this.z = z + from.z;
    }

    public void addVelocity(double x, double y, double z, ActiveNode from) {
        this.vx = x + from.vx;
        this.vy = y + from.vy;
        this.vz = z + from.vz;
    }

    public void addAcceleration(double x, double y, double z) {
        this.ax += x;
        this.ay += y;
        this.az += z;
    }

    public void clearAcceleration() {
        this.ax = 0;
        this.ay = 0;
        this.az = 0;
    }

    public void addForce(double x, double y, double z) {
        this.fx += x;
        this.fy += y;
        this.fz += z;
    }

    public void clearForce() {
        this.fx = 0;
        this.fy = 0;
        this.fz = 0;
    }

    public void integrate(float dt) {
        this.addAcceleration(this.fx * this.builtNode.oneByEffectiveMass, this.fy * this.builtNode.oneByEffectiveMass, this.fz * this.builtNode.oneByEffectiveMass);
        this.addVelocity(this.ax * dt, this.ay * dt, this.az * dt);
        if(!this.builtNode.isStatic) {
            this.addPosition(this.vx * dt, this.vy * dt, this.vz * dt);
        }
        this.clearForce();
        this.clearAcceleration();
    }

    public void integrate(float dt, ActiveNode from) {
        this.addAcceleration(this.fx * this.builtNode.oneByEffectiveMass, this.fy * this.builtNode.oneByEffectiveMass, this.fz * this.builtNode.oneByEffectiveMass);
        this.addVelocity(this.ax * dt, this.ay * dt, this.az * dt, from);
        if(!this.builtNode.isStatic) {
            this.addPosition(this.vx * dt, this.vy * dt, this.vz * dt, from);
        } else {
            this.addPosition(0, 0, 0, from);
        }
        this.clearForce();
        this.clearAcceleration();
    }

    @Override
    public void acceptForces(float dt) {
        this.integrate(dt);
    }

    public void acceptForces(float dt, ActiveNode from) {
        this.integrate(dt, from);
    }

    public Vec3d getPosition() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public void getPosition(float[] fill) {
        fill[0] = (float)this.x;
        fill[1] = (float)this.y;
        fill[2] = (float)this.z;
    }

    public void lerpPositions(ActiveNode from, ActiveNode to, float lerp) {
        float prel = 1 - lerp;
        this.x = from.x * prel + to.x * lerp;
        this.y = from.y * prel + to.y * lerp;
        this.z = from.z * prel + to.z * lerp;
    }
}
