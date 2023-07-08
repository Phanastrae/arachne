package phanastrae.arachne.screen.editor.tools;

import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.weave.Node;

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
