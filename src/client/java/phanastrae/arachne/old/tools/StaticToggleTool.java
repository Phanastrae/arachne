package phanastrae.arachne.old.tools;

import phanastrae.arachne.old.EditorMainScreen;
import phanastrae.old.Node;

public class StaticToggleTool implements ToolType {
    @Override
    public void onSwitchTo(EditorMainScreen mls) {
        mls.selection.clear();
    }

    @Override
    public void onClick(EditorMainScreen mls) {
        Node hn = mls.highlightedNode;
        if(hn != null) {
            hn.isStatic = !hn.isStatic;
            hn.clearVelocity();
        }
    }

    @Override
    public String getId() {
        return "staticToggle";
    }
}
