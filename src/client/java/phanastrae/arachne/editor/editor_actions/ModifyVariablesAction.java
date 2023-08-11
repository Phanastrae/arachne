package phanastrae.arachne.editor.editor_actions;

import net.minecraft.text.Text;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModifyVariablesAction<E, T> extends ModifiableAction {

    List<E> elements;
    List<T> oldValues;
    List<T> newValues;
    BiConsumer<E, T> setter;

    public ModifyVariablesAction(List<E> elements, List<T> oldValues, List<T> newValues, BiConsumer<E, T> setter) {
        this.elements = elements;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.setter = setter;
    }

    @Override
    public Text getTitle() {
        return Text.of("Modified Element" + (elements.isEmpty() ? "" : "s"));
    }

    @Override
    public void act(EditorInstance editorInstance) {
        for(int i = 0; i < this.elements.size(); i++) {
            if(i >= newValues.size()) return;
            this.setter.accept(this.elements.get(i), this.newValues.get(i));
        }
    }

    @Override
    public void undo(EditorInstance editorInstance) {
        for(int i = 0; i < this.elements.size(); i++) {
            if(i >= oldValues.size()) return;
            this.setter.accept(this.elements.get(i), this.oldValues.get(i));
        }
    }

    public static <E,T>ModifyVariablesAction<E, T> of(List<E> elements, Function<E, T> getter, BiFunction<E, Integer, T> newValueFunction, BiConsumer<E, T> setter) {
        ArrayList<T> oldValues = new ArrayList<>();
        ArrayList<T> newValues = new ArrayList<>();
        for(int i = 0; i < elements.size(); i++) {
            E e = elements.get(i);
            oldValues.add(getter.apply(e));
            newValues.add(newValueFunction.apply(e, i));
        }
        return new ModifyVariablesAction<>(elements, oldValues, newValues, setter);
    }
}
