package phanastrae.arachne.screen.widget;

import com.mojang.datafixers.util.Function7;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.weave.element.sketch.SketchElement;
import phanastrae.arachne.weave.element.sketch.SketchFace;
import phanastrae.arachne.weave.element.sketch.SketchPhysicsMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class PropertyHandler<E extends SketchElement> {

    List<PropertyWidget<E>> propertyWidgets = new ArrayList<>();
    List<Widget> children = new ArrayList<>();
    TextWidget counterHeader = null;

    Class<E> klass;
    EditorInstance editorInstance;
    TextRenderer textRenderer;
    int elementWidth;

    public PropertyHandler(Class<E> klass, EditorInstance editorInstance, TextRenderer textRenderer, int elementWidth) {
        this.klass = klass;
        this.editorInstance = editorInstance;
        this.textRenderer = textRenderer;
        this.elementWidth = elementWidth;
        setup();
    }

    abstract void setup();

    public <T>GenericPropertyWidget<T, E> addPropertyWidget(Function7<Function<E, T>, BiConsumer<E, T>, String, EditorInstance, TextRenderer, Integer, Integer, ? extends GenericPropertyWidget<T, E>> sup, Function<E, T> getter, BiConsumer<E, T> setter, String name, int height) {
        GenericPropertyWidget<T, E> gpw = sup.apply(getter, setter, name, this.editorInstance, this.textRenderer, this.elementWidth, height);
        this.addPropertyWidget(gpw);
        return gpw;
    }

    public BooleanPropertyWidget<E> addBooleanPropertyWidget(Function<E, Boolean> getter, BiConsumer<E, Boolean> setter, String name, int height) {
        BooleanPropertyWidget<E> pw = new BooleanPropertyWidget<>(getter, setter, name, this.editorInstance, this.textRenderer, this.elementWidth, height);
        this.addPropertyWidget(pw);
        return pw;
    }

    public ElementPropertyWidget<SketchPhysicsMaterial, E> addPhysicsMaterial(Function<E, SketchPhysicsMaterial> getter, BiConsumer<E, SketchPhysicsMaterial> setter, String name, int height, Supplier<List<SketchPhysicsMaterial>> listGetter) {
        ElementPropertyWidget<SketchPhysicsMaterial, E> pw = (ElementPropertyWidget<SketchPhysicsMaterial, E>)addPropertyWidget(ElementPropertyWidget::new, getter, setter, name, height);
        pw.listGetter = listGetter;
        pw.nameGetter = (el) -> el == null ? Text.empty() : Text.of(el.getName());
        return pw;
    }

    void putCounterHeader() {
        if(this.children.isEmpty()) {
            addLine();
        }
        this.counterHeader = new TextWidget(Text.empty(), this.textRenderer);
        this.counterHeader.alignLeft();
        this.children.add(this.counterHeader);
    }


    void addLine() {
        this.children.add(new DummyNewLineWidget());
    }

    void addSeparator() {
        this.children.add(new DummySeparatorWidget());
        addLine();
    }

    void addPropertyWidget(PropertyWidget<E> pw) {
        this.propertyWidgets.add(pw);

        if(!this.children.isEmpty()) {
            this.addLine();
        }

        TextWidget gpwName = new TextWidget(Text.translatable("arachne.editor.field.id." + pw.getName()), this.textRenderer);
        gpwName.setWidth((pw.getWidth() - 4) / 3);
        gpwName.alignLeft();
        this.children.add(gpwName);

        pw.setWidth((pw.getWidth() - 4) - gpwName.getWidth());
        this.children.add(pw);
    }

    public List<Widget> getChildren() {
        return this.children;
    }

    public void setList(List<E> list) {
        this.propertyWidgets.forEach((c) -> c.setList(list));
        if(this.counterHeader != null) {
            Text text = Text.translatable("arachne.editor.counter", list.size()).formatted(list.isEmpty() ? Formatting.RED : Formatting.GRAY);
            counterHeader.setMessage(text);
        }
        for(Widget w : this.children) {
            if(w instanceof TexturePreviewWidget tpw) {
                tpw.setList(list);
            }
        }
    }

    public void onSelectionChanged(List<SketchElement> selectionList) {
        ArrayList<E> list = new ArrayList<>();
        for(SketchElement sketchElement : selectionList) {
            if(this.klass.isInstance(sketchElement)) {
                E e = this.klass.cast(sketchElement);
                list.add(e);
            }
        }
        this.setList(list);
    }

    public void addChild(Widget w) {
        this.children.add(w);
    }
}
