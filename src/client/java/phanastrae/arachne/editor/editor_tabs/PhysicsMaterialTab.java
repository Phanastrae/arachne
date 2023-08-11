package phanastrae.arachne.editor.editor_tabs;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.screen.widget.ListGridWidget;
import phanastrae.arachne.screen.widget.PropertyEditorWidget;
import phanastrae.arachne.weave.element.sketch.*;

import java.util.ArrayList;
import java.util.List;

public class PhysicsMaterialTab extends EditorTab {

    public PhysicsMaterialTab(String id) {
        super(id);
    }

    @Override
    public List<Pair<Text, Runnable>> getActions(EditorInstance editorInstance) {
        List<Pair<Text, Runnable>> list = super.getActions(editorInstance);
        list.add(new Pair<>(Text.of("Add Physics Material"), editorInstance::addPhysicsMaterial));
        list.add(new Pair<>(Text.of("Delete Selected"), editorInstance::deleteSelected));
        return list;
    }

    @Override
    public void initScreen(EditorMainScreen screen) {
        PropertyEditorWidget propertyEditorWidget = screen.makePropertyEditorWidget();
        propertyEditorWidget.setup(List.of(SketchPhysicsMaterial.class), SketchPhysicsMaterial.class);
        screen.setPropertyEditorWidget(propertyEditorWidget);

        screen.setupListGridWidget(ListGridWidget.getButtons(() -> new ArrayList<>(screen.editorInstance.getSketchWeave().getPhysicsMaterials()), (el) -> {
            if(el == null) {
                return Text.empty();
            } else {
                return Text.of(el.getName());
            }
        }, screen::selectListGridElement, (el) -> !el.selected), false);
    }
}
