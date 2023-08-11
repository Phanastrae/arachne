package phanastrae.arachne.editor.editor_tabs;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.screen.editor.EditorMainScreen;

import java.util.List;

public class PreviewTab extends EditorTab {

    public PreviewTab(String id) {
        super(id);
    }

    @Override
    public List<Pair<Text, Runnable>> getActions(EditorInstance editorInstance) {
        List<Pair<Text, Runnable>> list = super.getActions(editorInstance);
        return list;
    }

    @Override
    public void initScreen(EditorMainScreen screen) {
        screen.setupWeaveInstance(true);
    }
}
