package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.editor_actions.ModifyVariableAction;
import phanastrae.arachne.weave.element.GTV;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class GenericPropertyWidget<T, E extends GTV> extends TextFieldWidget implements PropertyWidget<E> {

    EditorInstance editorInstance;
    @Nullable
    List<E> list;
    Function<E, T> getter;
    BiConsumer<E, T> setter;
    String name;

    String blankText = "";
    SelectionType oldType = SelectionType.EMPTY;
    @Nullable
    T oldValue = null;

    @Nullable
    ModifyVariableAction<E, T> lastAction = null;

    boolean sendUpdate;

    public GenericPropertyWidget(Function<E, T> getter, BiConsumer<E, T> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height) {
        this(getter, setter, name, editorInstance, textRenderer, width, height, true);
    }

    public GenericPropertyWidget(Function<E, T> getter, BiConsumer<E, T> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height, boolean sendUpdate) {
        super(textRenderer, 0, 0, width, height, Text.of(name));
        super.setChangedListener(this::onTextChanged);
        this.editorInstance = editorInstance;
        this.getter = getter;
        this.setter = setter;
        this.name = name;
        this.active = false;
        this.sendUpdate = sendUpdate;
        this.updateValue(true);
    }

    public enum SelectionType {
        EMPTY,
        SINGLE,
        MULTI
    }

    public abstract String varToString(T var);
    public abstract T stringToVar(String string);
    public abstract boolean charValid(char c);
    @Nullable
    public abstract T incrementBy(T var, double amount);
    public abstract boolean isStringValid(String string);

    @Override
    public void setList(@Nullable List<E> list) {
        this.list = list;
        this.update();
    }

    public void onTextChanged(String text) {
        if(oldType == SelectionType.EMPTY) return;
        if(text == null) return;

        if(!isStringValid(text)) return;
        T newValue = this.stringToVar(text);

        // if everything has the same value and no change has occurred, do nothing
        if(oldType == SelectionType.SINGLE) {
            if (newValue != null && newValue.equals(oldValue)) return;
            if (newValue == null && oldValue == null) return;
        }

        this.setValues(newValue);
    }

    public void setValues(T val) {
        if(this.list == null || this.list.isEmpty()) return;

        if(this.oldType == SelectionType.SINGLE || this.oldType == SelectionType.MULTI) {
            List<E> elements = new ArrayList<>();
            List<T> values = new ArrayList<>();
            for(E e : this.list) {
                T v = getter.apply(e);
                if(v == null && val == null) continue;
                if(v != null && v.equals(val)) continue;

                elements.add(e);
                values.add(v);
            }

            if(elements.isEmpty() || values.isEmpty()) return;

            if(lastAction != null && lastAction.canEdit()) {
                this.editorInstance.getActionQueue().updateLast(() -> lastAction.update(elements, values, val));
            } else {
                ModifyVariableAction<E, T> action = new ModifyVariableAction<>(elements, values, val, this.setter);
                if(this.sendUpdate) {
                    this.editorInstance.doAction(action);
                    this.lastAction = action;
                } else {
                    action.act(this.editorInstance);
                }
            }
            this.oldType = SelectionType.SINGLE;
            this.oldValue = val;
        }
    }

    public Text getEmptyText() {
        return Text.translatable("arachne.editor.field.id." + this.name);
    }

    public Text getMultiText() {
        return Text.translatable("arachne.editor.field.multiple");
    }

    public Text getNullText() {
        return Text.translatable("arachne.editor.field.none");
    }

    public void update() {
        updateValue();
        updateBox();
    }

    public void updateBox() {
        String text = this.getText();
        boolean textEmpty = text.isEmpty();

        if(textEmpty) {
            this.setSuggestion(this.blankText);
        } else {
            this.setSuggestion("");
        }

        if(isStringValid(text) || textEmpty) {
            this.setEditableColor(0x00FF00);
        } else {
            this.setEditableColor(0xFF0000);
        }
    }

    public void updateValue() {
        updateValue(false);
    }

    public void updateValue(boolean forceUpdate) {
        if(!forceUpdate && this.isFocused()) return;

        if(list == null || list.isEmpty()) {
            this.handleChange(SelectionType.EMPTY, null, forceUpdate);
        } else {
            SelectionType st = SelectionType.SINGLE;
            T v = getter.apply(list.get(0));

            // check if all are equal, if not set type as multi
            for (int i = 1; i < list.size(); i++) {
                if (!Objects.equals(getter.apply(list.get(i)), v)) {
                    st = SelectionType.MULTI;
                    v = null;
                    break;
                }
            }
            this.handleChange(st, v, forceUpdate);
        }
    }

    void handleChange(SelectionType type, @Nullable T value) {
        handleChange(type, value, false);
    }

    void handleChange(SelectionType type, @Nullable T value, boolean forceUpdate) {
        if(!forceUpdate && (type == oldType)) {
            if(value != null && value.equals(oldValue)) return;
            if(value == null && oldValue == null) return;
        }
        this.oldType = type;
        this.oldValue = value;

        String newBlankText = "";
        String newText = "";
        switch(type) {
            case EMPTY -> newBlankText = this.getEmptyText().getString();
            case SINGLE -> {
                String str = (value == null) ? "" : this.varToString(value);
                if(str != null) {
                    newText = str;
                } else {
                    newBlankText = this.getNullText().getString();
                }
            }
            case MULTI -> newBlankText = this.getMultiText().getString();
        }
        this.blankText = newBlankText;
        this.setText(newText);

        this.active = !(type == SelectionType.EMPTY);

        this.updateBox();
    }

    public boolean incrementValue(double amount) {
        T v1 = this.stringToVar(this.getText());
        T v2 = this.incrementBy(v1, amount);
        if(v2 == null) return false;
        this.setText(this.varToString(v2));
        return true;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setChangedListener(Consumer<String> changedListener) {
        // empty
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }
        if (this.isValidClickButton(button) && this.clicked(mouseX, mouseY)) {
            //this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            boolean alreadyFocused = this.isFocused();
            this.onClick(mouseX, mouseY);
            if(button == 0 & !alreadyFocused) {
                this.setCursorToEnd();
                this.setSelectionEnd(0);
            }
            if(button == 1) {
                this.setText("");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(incrementValue(amount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 0 || button == 1;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean bl = super.charTyped(chr, modifiers);
        update();
        return bl;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean bl = super.keyPressed(keyCode, scanCode, modifiers);
        update();
        return bl;
    }

    @Override
    public void tick() {
        update();
        super.tick();
    }

    @Override
    public void write(String text) {
        // strip banned characters
        IntStream chars = text.chars();
        IntStream filtered = chars.filter((i) -> charValid((char)i));
        int[] ia = filtered.toArray();
        char[] ca = new char[ia.length];
        for(int i = 0; i < ia.length; i++) {
            ca[i] = (char)ia[i];
        }
        super.write(String.valueOf(ca));
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if(!focused) {
            this.updateValue(true);
            this.setCursorToStart();
            this.setSelectionEnd(0);
            if(this.lastAction != null) {
                this.lastAction.finaliseEdit();
                this.lastAction = null;
            }
        }
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
