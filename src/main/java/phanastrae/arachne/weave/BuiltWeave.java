package phanastrae.arachne.weave;

import net.minecraft.util.math.Vec3d;
import phanastrae.arachne.weave.element.active.ForceAdder;
import phanastrae.arachne.weave.element.built.*;
import phanastrae.arachne.weave.element.sketch.SketchRenderMaterial;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BuiltWeave {

    BuiltNode[] nodes;
    BuiltEdge[] edges;
    BuiltFace[] faces;
    BuiltRigidBody[] rigidBodies;
    BuiltSettings settings;
    BuiltRenderLayer[] renderLayers;

    double minx;
    double miny;
    double minz;
    double maxx;
    double maxy;
    double maxz;

    public BuiltWeave(BuiltNode[] nodes, BuiltEdge[] edges, BuiltFace[] faces, BuiltRigidBody[] rigidBodies, BuiltSettings settings, BuiltRenderLayer[] renderLayers) {
        this.nodes = nodes;
        this.edges = edges;
        this.faces = faces;
        this.rigidBodies = rigidBodies;
        this.settings = settings;
        this.renderLayers = renderLayers;

        this.calculateDefaultBounds();
    }

    public void addForces(WeaveStateUpdater weaveStateUpdater, int tier) {
        for(ForceAdder forceAdder : this.nodes) {
            forceAdder.addForces(weaveStateUpdater);
        }
        for(ForceAdder forceAdder : this.edges) {
            forceAdder.addForces(weaveStateUpdater);
        }
        for(ForceAdder forceAdder : this.faces) {
            forceAdder.addForces(weaveStateUpdater);
        }
    }

    public void addWindForces(WeaveStateUpdater weaveStateUpdater, int multiplier, int tier) {
        Vec3d wind = weaveStateUpdater.weaveInstance.wind.multiply(this.settings.getWindMultiplier());
        for(BuiltFace face : this.faces) {
            face.applyWind(weaveStateUpdater, wind.x, wind.y, wind.z, 1.2, multiplier);
        }
    }

    public BuiltRenderLayer[] getRenderLayers() {
        return this.renderLayers;
    }

    public void calculateDefaultBounds() {
        this.minx = Double.POSITIVE_INFINITY;
        this.miny = Double.POSITIVE_INFINITY;
        this.minz = Double.POSITIVE_INFINITY;
        this.maxx = Double.NEGATIVE_INFINITY;
        this.maxy = Double.NEGATIVE_INFINITY;
        this.maxz = Double.NEGATIVE_INFINITY;

        for(BuiltNode node : this.nodes) {
            if(node.x < this.minx) {
                this.minx = node.x;
            }
            if(node.y < this.miny) {
                this.miny = node.y;
            }
            if(node.z < this.minz) {
                this.minz = node.z;
            }
            if(node.x > this.maxx) {
                this.maxx = node.x;
            }
            if(node.y > this.maxy) {
                this.maxy = node.y;
            }
            if(node.z > this.maxz) {
                this.maxz = node.z;
            }
        }
    }

    public double getSmallestEncompassingCubeWidth() {
        // get the width of the smallest cube, centered on 0,0,0, which encompasses every node in the weave
        double xLen = 2 * Math.max(minx, maxx);
        double yLen = 2 * Math.max(miny, maxy);
        double zLen = 2 * Math.max(minz, maxz);
        return Math.max(xLen, Math.max(yLen, zLen));
    }
}
