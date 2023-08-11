package phanastrae.arachne.editor.editor_tabs;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.screen.editor.EditorMainScreen;

import java.util.ArrayList;
import java.util.List;

public class EditorTab {

    final String id;

    public EditorTab(String id) {
        this.id = id;
    }

    public Text getName() {
        return Text.translatable("arachne.editor.tab." + id);
    }

    public String getId() {
        return this.id;
    }

    public List<Pair<Text, Runnable>> getActions(EditorInstance editorInstance) {
        return new ArrayList<>();
    }

    public void initScreen(EditorMainScreen screen) {
    }
}
