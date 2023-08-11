package phanastrae.arachne.editor.editor_actions;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.List;

public class ChangeSelectionAction extends ModifiableAction {

    @Nullable
    List<SketchElement> add;
    @Nullable
    List<SketchElement> remove;

    public ChangeSelectionAction(@Nullable List<SketchElement> add, @Nullable List<SketchElement> remove) {
        this.add = add;
        this.remove = remove;
    }

    @Override
    public Text getTitle() {
        int plus = 0;
        int minus = 0;
        if(this.add != null) {
            plus = this.add.size();
        }
        if(this.remove != null) {
            minus = this.remove.size();
        }
        if(plus > 0 && minus > 0) {
            return Text.of("Changed Selection (+" + plus + ", - " + minus + ")");
        } else if(plus > 0) {
            return Text.of("Changed Selection (+" + plus + ")");
        } else if(minus > 0) {
            return Text.of("Changed Selection (-" + minus + ")");
        } else {
            return Text.of("Changed Selection");
        }
    }

    @Override
    public void act(EditorInstance editorInstance) {
        if(this.add != null) {
            editorInstance.getSelectionManager().getSelection().addAll(this.add);
            for(SketchElement element : this.add) {
                element.selected = true;
            }
        }
        if(this.remove != null) {
            for(SketchElement element : this.remove) {
                element.selected = false;
            }
            editorInstance.getSelectionManager().getSelection().removeIf((e) -> !e.selected); // do this because removeAll can be slow for large selections
        }
        editorInstance.onSelectionChanged();
    }

    @Override
    public void undo(EditorInstance editorInstance) {
        if(this.remove != null) {
            editorInstance.getSelectionManager().getSelection().addAll(this.remove);
            for(SketchElement element : this.remove) {
                element.selected = true;
            }
        }
        if(this.add != null) {
            for(SketchElement element : this.add) {
                element.selected = false;
            }
            editorInstance.getSelectionManager().getSelection().removeIf((e) -> !e.selected); // do this because removeAll can be slow for large selections
        }
        editorInstance.onSelectionChanged();
    }
}
