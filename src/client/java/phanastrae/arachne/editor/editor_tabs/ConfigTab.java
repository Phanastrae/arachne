package phanastrae.arachne.editor.editor_tabs;

import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.screen.widget.PropertyEditorWidget;
import phanastrae.arachne.weave.element.sketch.SketchSettings;

import java.util.List;

public class ConfigTab extends EditorTab {

    public ConfigTab(String id) {
        super(id);
    }

    @Override
    public void initScreen(EditorMainScreen screen) {
        PropertyEditorWidget propertyEditorWidget = screen.makePropertyEditorWidget();
        propertyEditorWidget.setup(List.of(SketchSettings.class), SketchSettings.class);
        screen.setPropertyEditorWidget(propertyEditorWidget);
    }
}
