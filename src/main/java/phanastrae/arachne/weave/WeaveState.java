package phanastrae.arachne.weave;

import phanastrae.arachne.weave.element.active.ActiveNode;
import phanastrae.arachne.weave.element.active.ForceAcceptor;
import phanastrae.arachne.weave.element.built.BuiltNode;
import phanastrae.arachne.weave.element.sketch.SketchVertex;

import java.util.ArrayList;
import java.util.List;

public class WeaveState implements ForceAcceptor {

    ActiveNode[] nodes;

    ActiveNode dummyNode = new ActiveNode(new BuiltNode(new SketchVertex(null)));

    public WeaveState() {
        this(null);
    }

    public WeaveState(BuiltNode[] nodes) {
        if(nodes == null) {
            this.nodes = new ActiveNode[0];
        } else {
            int l = nodes.length;
            ActiveNode[] anodes = new ActiveNode[l];
            for (int i = 0; i < l; i++) {
                anodes[i] = new ActiveNode(nodes[i]);
            }
            this.nodes = anodes;
        }
    }

    @Override
    public void acceptForces(float dt) {
        for(ForceAcceptor forceAcceptor : this.nodes) {
            forceAcceptor.acceptForces(dt);
        }
    }

    public void acceptForces(float dt, WeaveState from) {
        if(this.nodes != null && from.nodes != null && this.nodes.length == from.nodes.length) {
            for(int i = 0; i < this.nodes.length; i++) {
                this.nodes[i].acceptForces(dt, from.nodes[i]);
            }
        }
    }

    public ActiveNode getNode(int i) {
        if(i < 0 || i >= this.nodes.length) {
            return dummyNode;
        } else {
            return this.nodes[i];
        }
    }

    public void lerp(WeaveState from, WeaveState to, float lerp) {
        if(this.nodes == null) return;
        int l = this.nodes.length;

        ActiveNode[] fromNodes = from.nodes;
        ActiveNode[] toNodes = to.nodes;
        if(fromNodes == null || toNodes == null) return;
        if(fromNodes.length != l || toNodes.length != l) return;

        for(int i = 0; i < l; i++) {
            this.nodes[i].lerpPositions(fromNodes[i], toNodes[i], lerp);
        }
    }
}
