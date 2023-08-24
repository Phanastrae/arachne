package phanastrae.arachne.weave.element.built;

import phanastrae.arachne.weave.element.active.ActiveNode;
import phanastrae.arachne.weave.element.active.ForceAdder;
import phanastrae.arachne.weave.WeaveStateUpdater;
import phanastrae.arachne.weave.element.sketch.SketchEdge;
import phanastrae.arachne.weave.element.sketch.SketchPhysicsMaterial;

public class BuiltEdge implements ForceAdder {
    public final int id;

    public final int startNode;
    public final int endNode;

    public final double length;
    public final double stiffness;
    public final boolean pullOnly;

    public BuiltEdge(SketchEdge edge) {
        this.id = edge.id;

        this.startNode = edge.start.id;
        this.endNode = edge.end.id;

        this.length = edge.length;
        SketchPhysicsMaterial physicsMaterial = edge.getPhysicsMaterial();
        this.stiffness = (physicsMaterial == null || edge.length == 0) ? 0 : physicsMaterial.elasticModulus * edge.virtualRadius * edge.virtualRadius * Math.PI / edge.length;
        this.pullOnly = edge.pullOnly;
    }

    @Override
    public void addForces(WeaveStateUpdater weaveStateUpdater) {
        this.applyTension(weaveStateUpdater);
    }

    public void applyTension(WeaveStateUpdater weaveStateUpdater) {
        ActiveNode node1 = weaveStateUpdater.getNodeInput(this.startNode);
        ActiveNode node2 = weaveStateUpdater.getNodeInput(this.endNode);
        double ox = node1.x - node2.x;
        double oy = node1.y - node2.y;
        double oz = node1.z - node2.z;
        double distance = Math.sqrt(ox*ox+oy*oy+oz*oz);
        if(this.pullOnly && distance <= length) return;
        double multiplier = this.stiffness * (1 - length / distance);
        ox *= multiplier;
        oy *= multiplier;
        oz *= multiplier;
        weaveStateUpdater.getNodeOutput(this.startNode).addForce(-ox, -oy, -oz);
        weaveStateUpdater.getNodeOutput(this.endNode).addForce(ox, oy, oz);
    }
}
