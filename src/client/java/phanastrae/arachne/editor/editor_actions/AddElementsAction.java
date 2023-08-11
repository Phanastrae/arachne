package phanastrae.arachne.editor.editor_actions;

import net.minecraft.text.Text;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.List;

public class AddElementsAction implements EditorAction {

    List<? extends SketchElement> elements;

    public AddElementsAction(List<? extends SketchElement> elements) {
        this.elements = elements;
    }

    @Override
    public Text getTitle() {
        return Text.of("Added (" + this.elements.size() + ") Elements");
    }

    @Override
    public void act(EditorInstance editorInstance) {
        editorInstance.getSketchWeave().addElements(this.elements);
    }

    @Override
    public void undo(EditorInstance editorInstance) {
        editorInstance.getSketchWeave().removeElements(this.elements);
    }
}
