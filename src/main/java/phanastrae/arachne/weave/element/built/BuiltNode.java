package phanastrae.arachne.weave.element.built;

import phanastrae.arachne.Arachne;
import phanastrae.arachne.weave.element.active.ForceAdder;
import phanastrae.arachne.weave.WeaveStateUpdater;
import phanastrae.arachne.weave.element.sketch.SketchPhysicsMaterial;
import phanastrae.arachne.weave.element.sketch.SketchVertex;

public class BuiltNode implements ForceAdder {
    public final int id;

    public final double x;
    public final double y;
    public final double z;

    public double effectiveMass;
    public double oneByEffectiveMass;

    public boolean isStatic; // TODO: should this be a thing???

    public BuiltNode(SketchVertex node) {
        this.id = node.id;
        this.x = node.x;
        this.y = node.y;
        this.z = node.z;
        this.effectiveMass = 0;
        SketchPhysicsMaterial physicsMaterial = node.getPhysicsMaterial();
        if(physicsMaterial != null) {
            this.addMass(physicsMaterial.density * node.virtualVolume);
        }
        this.isStatic = node.isStatic;
    }

    public void addMass(double d) {
        this.effectiveMass += d;
        this.oneByEffectiveMass = this.effectiveMass == 0 ? 0 : 1 / this.effectiveMass;
    }

    @Override
    public void addForces(WeaveStateUpdater weaveStateUpdater) {
        weaveStateUpdater.getNodeOutput(this.id).addAcceleration(0, -9.8 * weaveStateUpdater.getSettings().getGravityMultiplier(), 0);
    }
}
