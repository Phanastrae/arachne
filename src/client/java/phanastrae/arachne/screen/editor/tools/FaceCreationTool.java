package phanastrae.arachne.screen.editor.tools;

import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.weave.Face;
import phanastrae.arachne.weave.Node;

import java.util.ArrayList;
import java.util.Random;

public class FaceCreationTool implements ToolType {

    public ArrayList<Node> nodes = new ArrayList<>();

    @Override
    public void onSwitchTo(EditorMainScreen mls) {
        this.nodes.clear();
        if(mls.selection.getNodes().size() == 1) {
            Node selNode = mls.selection.getNodes().get(0);
            this.nodes.add(selNode);
        } else {
            mls.selection.clear();
        }
    }

    @Override
    public void onClick(EditorMainScreen mls) {
        if(mls.highlightedNode == null) return;
        if(!nodes.contains(mls.highlightedNode)) {
            nodes.add(mls.highlightedNode);
            mls.selection.clear();
            mls.selection.addNode(mls.highlightedNode);
        } else if(mls.highlightedNode == this.nodes.get(0) && nodes.size() >= 3) {
            Face face = new Face(this.nodes);
            // TODO: tweak
            Random random = new Random();
            int h = 0;
            for (Node node : nodes) {
                h += node.hashCode();
            }
            random.setSeed(h);
            face.r = random.nextInt(128) + 128;
            face.g = random.nextInt(128) + 128;
            face.b = random.nextInt(128) + 128;
            mls.phySys.faces.add(face);
            this.nodes.clear();
            mls.selection.clear();
        }
    }

    @Override
    public String getId() {
        return "faceCreation";
    }
}