package phanastrae.arachne.editor.editor_actions;

import net.minecraft.text.Text;
import phanastrae.arachne.editor.EditorInstance;

public interface EditorAction {

    Text getTitle();

    void act(EditorInstance editorInstance);
    void undo(EditorInstance editorInstance);
}
