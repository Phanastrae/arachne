package phanastrae.arachne.editor.editor_tabs;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.screen.widget.ListGridWidget;
import phanastrae.arachne.screen.widget.PropertyEditorWidget;
import phanastrae.arachne.weave.element.sketch.SketchRenderMaterial;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RenderMaterialTab extends EditorTab {

    public RenderMaterialTab(String id) {
        super(id);
    }

    @Override
    public List<Pair<Text, Runnable>> getActions(EditorInstance editorInstance) {
        List<Pair<Text, Runnable>> list = super.getActions(editorInstance);
        list.add(new Pair<>(Text.of("Add Render Material"), editorInstance::addRenderMaterial));
        list.add(new Pair<>(Text.of("Delete Selected"), editorInstance::deleteSelected));
        return list;
    }

    @Override
    public void initScreen(EditorMainScreen screen) {
        PropertyEditorWidget propertyEditorWidget = screen.makePropertyEditorWidget();
        propertyEditorWidget.setup(List.of(SketchRenderMaterial.class), SketchRenderMaterial.class);
        screen.setPropertyEditorWidget(propertyEditorWidget);

        screen.setupListGridWidget(ListGridWidget.getButtons(() -> new ArrayList<>(screen.editorInstance.getSketchWeave().getRenderMaterials()), (el) -> {
            if(el == null) {
                return Text.empty();
            } else {
                return Text.of(el.getName());
            }
        }, screen::selectListGridElement, (el) -> !el.selected), false);
    }
}
