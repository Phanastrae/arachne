package phanastrae.arachne.editor.editor_actions;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.weave.element.sketch.*;

import java.util.ArrayList;
import java.util.List;

public class RemoveElementsAction implements EditorAction {

    List<SketchElement> elements;

    public RemoveElementsAction(List<SketchElement> elements) {
        this.elements = elements;
    }

    @Override
    public Text getTitle() {
        return Text.of("Removed (" + this.elements.size() + ") Elements");
    }

    @Override
    public void act(EditorInstance editorInstance) {
        editorInstance.getSketchWeave().removeElements(this.elements);
    }

    @Override
    public void undo(EditorInstance editorInstance) {
        editorInstance.getSketchWeave().addElements(this.elements);
    }

    @Nullable
    public static EditorAction of(List<SketchElement> elements) {
        ArrayList<SketchElement> remove = new ArrayList<>();
        for(SketchElement e : elements) {
            if(e.getAdded() && e.canDelete()) {
                remove.add(e);
                if(e instanceof SketchTransform rb) {
                    rb.forAllChildrenInTree((r) -> {
                        if (r.getAdded() && r.canDelete()) {
                            remove.add(r);
                        }});
                }
            }
        }
        ArrayList<SketchEdge> vertexEdges = new ArrayList<>();
        for(SketchElement e : remove) {
            if(e instanceof SketchVertex n) {
                List<SketchEdge> edges = n.getChildren();
                if(edges != null) {
                    for (SketchEdge el : edges) {
                        if (el.getAdded() && el.canDelete()) {
                            vertexEdges.add(el);
                        }
                    }
                }
            }
        }
        remove.addAll(vertexEdges);
        ArrayList<SketchFace> edgeFaces = new ArrayList<>();
        for(SketchElement e : remove) {
            if(e instanceof SketchEdge edge) {
                List<SketchFace> faces = edge.getChildren();
                if(faces != null) {
                    for (SketchFace el : faces) {
                        if (el.getAdded() && el.canDelete()) {
                            edgeFaces.add(el);
                        }
                    }
                }
            }
        }
        remove.addAll(edgeFaces);
        if(remove.isEmpty()) return null;
        ArrayList<SketchElement> deSelect = new ArrayList<>();
        for(SketchElement e : remove) {
            if(e.selected) {
                deSelect.add(e);
            }
        }
        EditorAction actRemove = new RemoveElementsAction(remove);
        if(deSelect.isEmpty()) {
            return actRemove;
        } else {
            EditorAction actDeselect = new ChangeSelectionAction(null, deSelect);
            return new CompositeAction(List.of(actDeselect, actRemove), actRemove.getTitle());
        }
    }
}