package phanastrae.arachne.weave;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.weave.link_type.Link;
import phanastrae.arachne.screen.editor.tools.DragTool;
import phanastrae.arachne.screen.editor.tools.Selection;
import phanastrae.arachne.util.CenteredPlane;
import phanastrae.arachne.util.Line;

import java.util.Random;

public class PhysicsSystem extends WeaveTickable {

    public boolean windActive = false;
    public boolean doFloor = false;
    public float maxDistance = 60;
    @Nullable
    public Vec3d lastPos = null;

    public PhysicsSystem() {
        super();
    }

    public PhysicsSystem(NbtCompound nbt) {
        super(nbt);
    }

    public void storeLastPositions() {
        for(Node node : this.nodes) {
            node.lastPos = node.pos;
        }
    }

    @Override
    public void tick(double dt, int steps) {
        this.storeLastPositions();
        if(steps < 1) return;
        double dt2 = dt / steps;
        for(int i = 0; i < steps; i++) {
            this.doTick(dt2);
        }
    }

    @Override
    public void tick(double dt) {
        this.tick(dt, 1);
    }

    public void doTick(double dt) {
        for(Link link : this.links) {
            link.tickLink(dt);
        }

        Selection selection = null;
        EditorMainScreen.ToolContainer toolContainer = null;
        EditorMainScreen.SelectionMode selectionMode = EditorMainScreen.SelectionMode.VERTEX;
        if(MinecraftClient.getInstance().currentScreen instanceof EditorMainScreen mls) {
            selection = mls.selection;
            toolContainer = mls.toolContainer;
            selectionMode = mls.selectionMode;
        }

        // apply wind
        double t = 0;
        if (!(MinecraftClient.getInstance().world == null)) {
            t = MinecraftClient.getInstance().world.getTime() / 20f; //TODO: really probably shouldn't be using this
        }
        Random random = new Random();
        Vec3d windVelocityBase = Vec3d.ZERO;
        if(windActive) {
            windVelocityBase = new Vec3d(- 5 + Math.sin(t / 16) * Math.sin(t / 7) * 3, Math.sin(t / 3.314) * 2, Math.cos(t / 16) * Math.sin(t / 7) * 8);
            //windVelocityBase = new Vec3d(80, 40, 20);
        }
        for (Face face : this.faces) {
            double vx = 0;
            double vy = 0;
            double vz = 0;
            for (Node n : face.nodes) { // TODO: optimise
                vx += n.velocity.x;
                vy += n.velocity.y;
                vz += n.velocity.z;
            }
            vx /= face.nodes.length;
            vy /= face.nodes.length;
            vz /= face.nodes.length; // TODO: should this be average per sub-face instead? maybe??
            Vec3d p3 = face.getCenterPos();
            for (int i = 0; i < face.nodes.length; i++) {
                // add a bit of randomness
                Vec3d windVelocity = windVelocityBase.multiply(0.9 + 0.2 * random.nextDouble(), 0.9 + 0.2 * random.nextDouble(), 0.9 + 0.2 * random.nextDouble());
                Node n1 = face.nodes[i];
                Node n2 = face.nodes[(i + 1) % face.nodes.length];

                Vec3d relativeVelocity = new Vec3d(vx - windVelocity.x, vy - windVelocity.y, vz - windVelocity.z);
                if (relativeVelocity.lengthSquared() < 1E-5) continue;

                // project the points onto a plane with normal = relativeVelocity
                // TODO: optimise
                // TODO: stop assuming uniform(ish) wind that flows through everything i.e. non-air-exposed faces should not experience wind
                CenteredPlane plane = new CenteredPlane(Vec3d.ZERO, relativeVelocity);
                Vec3d p1 = n1.pos;
                Vec3d p2 = n2.pos;
                Line l1 = new Line(p1, relativeVelocity);
                Line l2 = new Line(p2, relativeVelocity);
                Line l3 = new Line(p3, relativeVelocity);
                Vec3d q1 = plane.intersectLine(l1, Double.NEGATIVE_INFINITY);
                Vec3d q2 = plane.intersectLine(l2, Double.NEGATIVE_INFINITY);
                Vec3d q3 = plane.intersectLine(l3, Double.NEGATIVE_INFINITY);
                if (q1 == null || q2 == null || q3 == null) continue;
                // calculate area of projected triangle
                // TODO: check this formula is correct
                Vec3d q12 = q1.subtract(q2);
                Vec3d q13 = q1.subtract(q3);
                double area = q13.crossProduct(q12).length() / 2;
                // calculate force
                // TODO: check maths
                // F = -2 * D * A * |v-V| * ((v-V).N) * N
                double fluidDensity = 1.2;
                Vec3d normal = Face.getNormal(p1, p2, p3);
                if(normal.dotProduct(relativeVelocity) < 0) { // make sure normal is correctly oriented
                    normal.multiply(-1);
                }
                double mag = -2 * fluidDensity * area * relativeVelocity.length() * relativeVelocity.dotProduct(normal);
                Vec3d force = normal.multiply(mag);
                // apply force
                n1.addForce(force.multiply(0.5));
                n2.addForce(force.multiply(0.5));
            }
        }

        for(Node node : this.nodes) {
            if(!node.isStatic && !(selectionMode == EditorMainScreen.SelectionMode.VERTEX && toolContainer != null && toolContainer.tool instanceof DragTool dragTool && dragTool.lastMouseRay != null && selection != null && selection.contains(node))) {
                // apply gravity
                node.addAcceleration(new Vec3d(0, -9.8, 0)); //TODO: tweak grav value
                /*
                if(this.windActive) {
                    // apply wind //TODO: change how this works, also would probably eventually want it to be based on faces maybe partly?? and velocity? perhaps? idk?
                    double t = 0;
                    if (!(MinecraftClient.getInstance().world == null)) {
                        t = MinecraftClient.getInstance().world.getTime() / 20f; //TODO: really probably shouldn't be using this
                    }
                    Vec3d windForce = new Vec3d(Math.sin(t / 4) * Math.sin(t / 1.2) * 8, Math.sin(t / 3.314) * 2, Math.cos(t / 4) * Math.sin(t / 1.2) * 8);
                    node.addAcceleration(windForce);
                }
                */
                // apply air resistance // TODO: should wind replace this? maybe?
                node.velocity = node.velocity.multiply(Math.exp(-dt * 2));
                node.velocity = node.velocity.multiply(Math.exp(-dt * node.velocity.lengthSquared() * 0.05f)); //TODO: this is nonsense i made up, change
                // apply acceleration and velocity
                node.integrate(dt);
                // floor and boundary collision
                if(node.pos.y < -0.49f && doFloor) {
                    node.pos = new Vec3d(node.pos.x, -0.49f, node.pos.z); //TODO: change how this works
                    if(node.velocity.y < 0) {
                        node.velocity = node.velocity.subtract(0, node.velocity.y, 0);
                    }
                }
                if(node.pos.lengthSquared() > this.maxDistance*this.maxDistance) { // keeps node in ball TODO: make better
                    node.pos = node.pos.multiply(this.maxDistance/node.pos.length());
                    node.velocity = new Vec3d(0, 0, 0); // really this should set the velocity tangent to the surface to 0 probably but eh
                }
            }
        }
    }

    public void translateNonStaticNodes(Vec3d by) {
        for(Node node : this.nodes) {
            if(!node.isStatic) {
                node.pos = node.pos.add(by);
                node.lastPos = node.lastPos.add(by);
            }
        }
    }
}
