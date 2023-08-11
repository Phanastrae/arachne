package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.editor_actions.ModifyVariableAction;
import phanastrae.arachne.weave.element.GTV;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BooleanPropertyWidget<E extends GTV> extends PressableWidget implements Tickable, PropertyWidget<E> {

    @Nullable
    E element;

    @Nullable
    List<E> list;

    TextRenderer textRenderer;
    EditorInstance editorInstance;
    Function<E, Boolean> getter;
    BiConsumer<E, Boolean> setter;
    String name;

    @Nullable
    BooleanPropertyWidget.BoolState oldValue = BooleanPropertyWidget.BoolState.EMPTY;

    @Nullable
    ModifyVariableAction<E, Boolean> lastAction = null;

    boolean sendUpdate;

    public BooleanPropertyWidget(Function<E, Boolean> getter, BiConsumer<E, Boolean> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height) {
        this(getter, setter, name, editorInstance, textRenderer, width, height, true);
    }

    public BooleanPropertyWidget(Function<E, Boolean> getter, BiConsumer<E, Boolean> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height, boolean sendUpdate) {
        super(0, 0, width, height, Text.of(name));
        this.editorInstance = editorInstance;
        this.textRenderer = textRenderer;
        this.getter = getter;
        this.setter = setter;
        this.name = name;
        this.active = false;
        this.sendUpdate = sendUpdate;
        this.updateValue(true);
    }

    enum BoolState {
        EMPTY,
        TRUE,
        FALSE,
        MIXED
    }

    public void update() {
        updateValue();
    }

    public void updateValue() {
        updateValue(false);
    }

    @Override
    public void onPress() {
        if(oldValue == null || oldValue == BooleanPropertyWidget.BoolState.EMPTY) return;

        boolean newVal = oldValue != BooleanPropertyWidget.BoolState.TRUE;
        this.setValues(newVal);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // TODO
    }

    @Override
    public void tick() {
        update();
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        int col;
        String s;
        if(this.oldValue == null) return;
        switch (this.oldValue) {
            default -> {col = 0xFFFFFFFF; s = "";}
            case TRUE -> {col = 0xFF00FF00; s = "True";}
            case FALSE -> {col = 0xFFFF0000; s = "False";}
            case MIXED -> {col = 0xFFFFFF00; s = "Mixed";}
        }

        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xEF00003F);
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, col);

        if(!s.equals("")) {
            context.drawText(this.textRenderer, Text.of(s), this.getX() + 3, this.getY()+1, col, false);
        }
    }

    public void updateValue(boolean forceUpdate) {
        if(list == null || list.isEmpty()) {
            this.handleChange(BoolState.EMPTY, forceUpdate);
        } else {
            GenericPropertyWidget.SelectionType st = GenericPropertyWidget.SelectionType.SINGLE;
            Boolean v = getter.apply(list.get(0));

            // check if all are equal, if not set type as multi
            for (int i = 1; i < list.size(); i++) {
                if (!Objects.equals(getter.apply(list.get(i)), v)) {
                    st = GenericPropertyWidget.SelectionType.MULTI;
                    v = null;
                    break;
                }
            }
            BoolState boolState;
            switch (st) {
                case MULTI -> boolState = BoolState.MIXED;
                case SINGLE -> boolState = v ? BoolState.TRUE : BoolState.FALSE;
                default -> boolState = BoolState.EMPTY;
            }
            this.handleChange(boolState, forceUpdate);
        }
    }

    void handleChange(BoolState value, boolean forceUpdate) {
        if(!forceUpdate) {
            if(value != null && value.equals(oldValue)) return;
            if(value == null && oldValue == null) return;
        }
        this.oldValue = value;

        this.active = !(value == BoolState.EMPTY);
    }

    public void setValues(boolean val) {
        if(this.list == null || this.list.isEmpty()) return;

        List<E> elements = new ArrayList<>();
        List<Boolean> values = new ArrayList<>();
        for(E e : this.list) {
            Boolean v = getter.apply(e);
            boolean oldVal = v != null && v;
            if(oldVal == val) continue;

            elements.add(e);
            values.add(v);
        }

        if(elements.isEmpty() || values.isEmpty()) return;

        if(lastAction != null && lastAction.canEdit()) {
            this.editorInstance.getActionQueue().updateLast(() -> lastAction.update(elements, values, val));
        } else {
            ModifyVariableAction<E, Boolean> action = new ModifyVariableAction<>(elements, values, val, this.setter);
            if(sendUpdate) {
                this.editorInstance.doAction(action);
            } else {
                action.act(this.editorInstance);
            }
            this.lastAction = action;
        }
        this.oldValue = val ? BoolState.TRUE : BoolState.FALSE;
    }

    @Override
    public void setList(@Nullable List<E> list) {
        this.list = list;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
