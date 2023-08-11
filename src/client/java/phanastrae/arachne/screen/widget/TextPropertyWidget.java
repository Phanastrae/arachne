package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class TextPropertyWidget<E extends SketchElement> extends GenericPropertyWidget<String, E> {

    public TextPropertyWidget(Function<E, String> getter, BiConsumer<E, String> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height) {
        super(getter, setter, name, editorInstance, textRenderer, width, height);
    }

    @Override
    public String varToString(String var) {
        return var;
    }

    @Override
    public String stringToVar(String string) {
        return string;
    }

    @Override
    public boolean charValid(char c) {
        return true;
    }

    @Override
    public boolean isStringValid(String string) {
        return true;
    }

    @Override
    public String incrementBy(String var, double amount) {
        return null;
    }
}
