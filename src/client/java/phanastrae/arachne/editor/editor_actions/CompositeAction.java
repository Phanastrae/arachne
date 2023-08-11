package phanastrae.arachne.editor.editor_actions;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.editor.EditorInstance;

import java.util.List;

public class CompositeAction extends ModifiableAction {

    List<EditorAction> actions;

    @Nullable
    Text text;

    public CompositeAction(List<EditorAction> actions) {
        this(actions, null);
    }
    public CompositeAction(List<EditorAction> actions, @Nullable Text text) {
        this.actions = actions;
        this.text = text;
    }

    @Override
    public Text getTitle() {
        return this.text == null ? Text.of("Multiple Actions") : text;
    }

    public void setTitle(Text text) {
        this.text = text;
    }

    @Override
    public void act(EditorInstance editorInstance) {
        for(int i = 0; i < this.actions.size(); i++) {
            this.actions.get(i).act(editorInstance);
        }
    }

    @Override
    public void undo(EditorInstance editorInstance) {
        for(int i = this.actions.size()-1; i >= 0; i--) {
            this.actions.get(i).undo(editorInstance);
        }
    }

    @Override
    public void finaliseEdit() {
        for(EditorAction action : this.actions) {
            if(action instanceof ModifiableAction maction) {
                maction.finaliseEdit();
            }
        }
        super.finaliseEdit();
    }

    public boolean addAction(EditorAction action) {
        if(this.canEdit()) {
            return this.actions.add(action);
        } else {
            return false;
        }
    }
}
