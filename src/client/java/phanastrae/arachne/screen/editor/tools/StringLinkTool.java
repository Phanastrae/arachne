package phanastrae.arachne.screen.editor.tools;

import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.weave.link_type.StringLink;

public class StringLinkTool implements ToolType {
    @Override
    public void onClick(EditorMainScreen mls) {
        // TODO: some sort of crash?
        if(mls.selection.getNodes().isEmpty() && mls.highlightedNode != null) {
            mls.selection.addNode(mls.highlightedNode);
        } else {
            if(mls.selection.getNodes().size() == 1) {
                if(mls.highlightedNode != null && !mls.selection.contains(mls.highlightedNode)) {
                    mls.phySys.links.add(new StringLink(mls.selection.getNodes().get(0), mls.highlightedNode));
                    mls.selection.clear();
                }
            }
        }
    }

    @Override
    public String getId() {
        return "stringLink";
    }
}