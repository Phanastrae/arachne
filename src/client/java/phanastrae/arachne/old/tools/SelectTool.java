package phanastrae.arachne.old.tools;

import net.minecraft.util.math.Vec3d;
import phanastrae.old.Face;
import phanastrae.arachne.old.EditorMainScreen;
import phanastrae.old.Node;
import phanastrae.old.link_type.Link;
import phanastrae.arachne.util.Line;

public class SelectTool implements ToolType {

    Highlight highlight;

    // TODO: clear on open and/or close
    // maybe treat switch as a close action? or not idk.

    @Override
    public void onTick(EditorMainScreen mls) {
        // update highlight region
        if(this.highlight != null) {
            this.highlight.setEnd(mls.lastScreenSpaceMouseX, mls.lastScreenSpaceMouseY);
        }
    }

    @Override
    public void onClick(EditorMainScreen mls) {
        if(this.highlight == null && mls.highlightedNode != null) {
            mls.selection.clear();
            mls.selection.addNode(mls.highlightedNode);
        } else {
            Line ray = mls.mouseRay;
            Face hitFace = null;
            double maxDistance = Double.POSITIVE_INFINITY;
            for(Face face : mls.phySys.faces) { // TODO: make not bad
                Vec3d hit = face.rayHit(ray);
                if(hit != null) {
                    double d = hit.subtract(ray.point).lengthSquared();
                    if(d < maxDistance) {
                        maxDistance = d;
                        hitFace = face;
                    }
                }
            }
            if(hitFace != null) {
                mls.selection.clear();
                mls.selection.addFace(hitFace);
                return;
            }
            // TODO: consider changing how box creation works when clicking a node
            this.highlight = new Highlight(mls.lastScreenSpaceMouseX, mls.lastScreenSpaceMouseY);
        }
    }

    @Override
    public void onRelease(EditorMainScreen mls) {
        // end highlight region
        if(this.highlight != null) {
            this.highlight.setEnd(mls.lastScreenSpaceMouseX, mls.lastScreenSpaceMouseY);
            Selection selection = new Selection();
            // TODO: select all objects of type in region
            // TODO: consider changing
            // probably want to either use the calced ss pos (might want to phase that out)
            // or just calc the perspective box and check whether nodes are inside it
            // maybe an AABB around it too idk if that would help or not but perhaps
            Vec3d min = this.highlight.getMinPos();
            Vec3d max = this.highlight.getMaxPos();
            for(Node node : mls.phySys.nodes) {
                if(min.x <= node.posScreenSpace.x && node.posScreenSpace.x <= max.x) {
                    if(min.y <= node.posScreenSpace.y && node.posScreenSpace.y <= max.y) {
                        selection.addNode(node);
                    }
                }
            }
            for(Link edge : mls.phySys.links) {
                if(selection.contains(edge.node1) && selection.contains(edge.node2)) {
                    selection.addEdge(edge);
                }
            }
            for(Face face : mls.phySys.faces) { // TODO: optimise & make less bad
                boolean inSelection = true;
                for(Node node : face.nodes) {
                    if(!selection.contains(node)) {
                        inSelection = false;
                        break;
                    }
                }
                if(inSelection) {
                    selection.addFace(face);
                }
            }
            // TODO: add different modes ie replace, union, intersect
            mls.selection.clear();
            mls.selection.addSelection(selection);
            this.highlight = null;
        }
    }

    public Highlight getHighlight() {
        return this.highlight;
    }

    @Override
    public String getId() {
        return "select";
    }
}
