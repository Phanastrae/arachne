package phanastrae.arachne.screen.widget;

import com.mojang.datafixers.util.Function7;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.tools.EditorTool;
import phanastrae.arachne.weave.element.sketch.SketchElement;
import phanastrae.arachne.weave.element.sketch.SketchPhysicsMaterial;
import phanastrae.arachne.weave.element.sketch.SketchRenderMaterial;
import phanastrae.old.RenderMaterial;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ToolSettingsWidget extends ScrollableSubWindowWidget {

    EditorInstance editorInstance;
    @Nullable
    EditorTool selectedTool = null;

    public ToolSettingsWidget(EditorInstance editorInstance, TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
        super(textRenderer, x, y, width, height, message);
        this.editorInstance = editorInstance;
    }

    public void setTool(@Nullable EditorTool tool) {
        setTool(tool, false);
    }

    public void setTool(@Nullable EditorTool tool, boolean force) {
        if(tool != this.selectedTool || force) {
            this.setFocused(null);
            this.selectedTool = tool;
            this.clearChildren();
            this.active = tool != null;
            this.visible = this.active;
            if(tool != null) {
                tool.setup(this);
            }
            this.positionChildren();
            this.tick();
        }
    }

    public <T extends EditorTool>BooleanPropertyWidget<T> addBoolean(T tool, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter, String name) {
        BooleanPropertyWidget<T> bpw = new BooleanPropertyWidget<>(getter, setter, name, this.editorInstance, this.textRenderer, this.getInteriorWidth(), 10, false);
        bpw.setElement(tool);
        addField(bpw);
        return bpw;
    }

    public <T extends EditorTool>DoublePropertyWidget<T> addDouble(T tool, Function<T, Double> getter, BiConsumer<T, Double> setter, String name) {
        DoublePropertyWidget<T> bpw = new DoublePropertyWidget<>(getter, setter, name, this.editorInstance, this.textRenderer, this.getInteriorWidth(), 10, false);
        bpw.setElement(tool);
        addField(bpw);
        return bpw;
    }

    public <T extends EditorTool>IntegerPropertyWidget<T> addInteger(T tool, Function<T, Integer> getter, BiConsumer<T, Integer> setter, String name) {
        IntegerPropertyWidget<T> bpw = new IntegerPropertyWidget<>(getter, setter, name, this.editorInstance, this.textRenderer, this.getInteriorWidth(), 10, false);
        bpw.setElement(tool);
        addField(bpw);
        return bpw;
    }

    public <T extends EditorTool>ElementPropertyWidget<SketchRenderMaterial, T> addRenderMaterial(T tool, Function<T, SketchRenderMaterial> getter, BiConsumer<T, SketchRenderMaterial> setter, String name, Supplier<List<SketchRenderMaterial>> listGetter) {
        ElementPropertyWidget<SketchRenderMaterial, T> bpw = new ElementPropertyWidget<>(getter, setter, name, this.editorInstance, this.textRenderer, this.getInteriorWidth(), 10, false);
        bpw.setElement(tool);
        addField(bpw);
        bpw.listGetter = listGetter;
        bpw.nameGetter = (el) -> el == null ? Text.empty() : Text.of(el.getName());
        return bpw;
    }

    public void addField(PropertyWidget w) {
        TextWidget gpwName = new TextWidget(Text.translatable("arachne.editor.field.id." + w.getName()), this.textRenderer);
        gpwName.setWidth((w.getWidth() - 4) / 3);
        gpwName.alignLeft();
        this.children.add(gpwName);

        w.setWidth((w.getWidth() - 4) - gpwName.getWidth());
        this.children.add(w);
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 10;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // TODO
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    public EditorInstance getEditorInstance() {
        return this.editorInstance;
    }
}
