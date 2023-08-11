package phanastrae.arachne.weave;

import net.minecraft.util.math.Vec3d;
import phanastrae.arachne.weave.element.active.ForceAdder;
import phanastrae.arachne.weave.element.built.*;
import phanastrae.arachne.weave.element.sketch.SketchRenderMaterial;

import java.util.HashMap;
import java.util.Random;

public class BuiltWeave {

    BuiltNode[] nodes;
    BuiltEdge[] edges;
    BuiltFace[] faces;
    BuiltRigidBody[] rigidBodies;
    BuiltSettings settings;
    BuiltRenderLayer[] renderLayers;

    public BuiltWeave(BuiltNode[] nodes, BuiltEdge[] edges, BuiltFace[] faces, BuiltRigidBody[] rigidBodies, BuiltSettings settings, BuiltRenderLayer[] renderLayers) {
        this.nodes = nodes;
        this.edges = edges;
        this.faces = faces;
        this.rigidBodies = rigidBodies;
        this.settings = settings;
        this.renderLayers = renderLayers;
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
}
