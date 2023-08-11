package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.weave.element.GTV;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ElementPropertyWidget<T extends SketchElement, E extends GTV> extends GenericPropertyWidget<T, E> {

    public Function<T, Text> nameGetter;
    public Supplier<List<T>> listGetter;

    public ElementPropertyWidget(Function<E, T> getter, BiConsumer<E, T> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height) {
        super(getter, setter, name, editorInstance, textRenderer, width, height);
    }

    public ElementPropertyWidget(Function<E, T> getter, BiConsumer<E, T> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height, boolean sendUpdate) {
        super(getter, setter, name, editorInstance, textRenderer, width, height, sendUpdate);
    }

    @Override
    public String varToString(T var) {
        if(nameGetter == null) {
            return var == null ? "none" : var.getTypeName().getString();
        } else {
            return nameGetter.apply(var).getString();
        }
    }

    public Text getText(T var) {
        if(nameGetter == null) {
            return Text.of(var == null ? "none" : var.getTypeName().getString());
        } else {
            return nameGetter.apply(var);
        }
    }

    @Override
    public T stringToVar(String string) {
        return null;
    }

    @Override
    public boolean charValid(char c) {
        return false;
    }

    @Override
    public @Nullable T incrementBy(T var, double amount) {
        return null;
    }

    @Override
    public boolean isStringValid(String string) {
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // empty
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || !this.clicked(mouseX, mouseY)) {
            return false;
        }
        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.openWindow();
            return true;
        } else if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.clear();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            this.clear();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onTextChanged(String text) {
        // empty
    }

    @Override
    public void setValues(T val) {
        super.setValues(val);
    }

    public void clear() {
        this.setValues(null);
        this.updateValue(true);
    }

    public void openWindow() {
        EditorMainScreen screen = this.editorInstance.getScreen();
        screen.setupListGridWidget(ListGridWidget.getButtons(listGetter == null ? List::of : listGetter, this::getText, (el) -> {
            this.setValues(el);
            this.updateValue(true);
            screen.setupListGridWidget(null, false);
        }, (el) -> el != this.oldValue), true);
    }

    @Override
    public void setFocused(boolean focused) {
        if(!focused) {
            super.setFocused(false);
        }
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
