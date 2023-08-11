package phanastrae.old;

import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class Node {
    int id = -1;

    public Vec3d pos;
    public Vec3d lastPos;
    public Vec3d velocity;
    public Vec3d storedAcceleration;
    public Vec3d posScreenSpace;
    public double mass;
    public boolean isStatic;

    public Node(Vec3d position) {
        this(1.0, position);
    }

    public Node(double mass, Vec3d position) {
        this.mass = mass;
        this.pos = position;
        this.lastPos = this.pos;
        this.posScreenSpace = new Vec3d(0, 0, 0);
        this.velocity = new Vec3d(0, 0, 0);
        this.storedAcceleration = new Vec3d(0, 0, 0);
        this.isStatic = true; //TODO: add in constructor?
    }

    public void addForce(Vec3d force) {
        if(this.mass == 0) return; //TODO: is this ideal? does it matter?
        this.addAcceleration(force.multiply(1/this.mass));
    }

    public void addAcceleration(Vec3d acceleration) {
        this.storedAcceleration = this.storedAcceleration.add(acceleration);
    }

    public void integrate(double dt) {
        this.velocity = this.velocity.add(this.storedAcceleration.multiply(dt));
        this.storedAcceleration = new Vec3d(0, 0, 0);
        //this.lastPos = this.pos; // TODO: should this be here?
        this.pos = this.pos.add(this.velocity.multiply(dt));
    }

    public void updatePosScreenSpace(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        Vector4f v = new Vector4f((float)(this.pos.x), (float)(this.pos.y), (float)(this.pos.z), 1);
        v = v.mul(viewMatrix);
        v = v.mulProject(projectionMatrix);
        this.posScreenSpace = new Vec3d(v.x, v.y, v.z);
    }

    public boolean isVisible() {
        return posScreenSpace.z <= 1;
    }

    public void clearVelocity() {
        this.storedAcceleration = new Vec3d(0, 0, 0);
        this.velocity = new Vec3d(0, 0, 0);
    }

    public Vec3d getPos() {
        return this.pos;
    }

    public Vec3d getPos(float tickDelta) {
        if(tickDelta <= 0) {
            return lastPos;
        } else if(tickDelta >= 1) {
            return pos;
        } else {
            return lastPos.add(pos.subtract(lastPos).multiply(tickDelta));
        }
    }

    public void setPos(double x, double y, double z) {
        this.pos = new Vec3d(x, y, z);
    }

    public void setX(double x) {
        this.setPos(x, this.pos.y, this.pos.z);
    }

    public void setY(double y) {
        this.setPos(this.pos.x, y, this.pos.z);
    }

    public void setZ(double z) {
        this.setPos(this.pos.x, this.pos.y, z);
    }

    public double getX() {
        return this.pos.x;
    }

    public double getY() {
        return this.pos.y;
    }

    public double getZ() {
        return this.pos.z;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public double getMass() {
        return this.mass;
    }
}
