package phanastrae.arachne.editor.editor_tabs;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.ToolBarWidget;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.screen.widget.PropertyEditorWidget;
import phanastrae.arachne.screen.widget.SketchStructureViewWidget;
import phanastrae.arachne.screen.widget.ToolSettingsWidget;
import phanastrae.arachne.weave.element.sketch.SketchEdge;
import phanastrae.arachne.weave.element.sketch.SketchFace;
import phanastrae.arachne.weave.element.sketch.SketchTransform;
import phanastrae.arachne.weave.element.sketch.SketchVertex;

import java.util.List;

public class MainTab extends EditorTab {

    public MainTab(String id) {
        super(id);
    }

    @Override
    public List<Pair<Text, Runnable>> getActions(EditorInstance editorInstance) {
        List<Pair<Text, Runnable>> list = super.getActions(editorInstance);
        //list.add(new Pair<>(Text.of("Add Transform"), editorInstance::addTransform));
        //list.add(new Pair<>(Text.of("Add Vertex Collection"), editorInstance::addVertexCollection));
        list.add(new Pair<>(Text.of("Add Vertex"), editorInstance::addVertex));
        list.add(new Pair<>(Text.of("Delete Selected"), editorInstance::deleteSelected));
        return list;
    }

    @Override
    public void initScreen(EditorMainScreen screen) {
        PropertyEditorWidget propertyEditorWidget = screen.makePropertyEditorWidget();
        //propertyEditorWidget.setup(List.of(SketchVertex.class, SketchEdge.class, SketchFace.class, SketchTransform.class), SketchVertex.class);
        propertyEditorWidget.setup(List.of(SketchVertex.class, SketchEdge.class, SketchFace.class), SketchVertex.class);
        screen.setPropertyEditorWidget(propertyEditorWidget);

        screen.initToolBar();
    }
}
