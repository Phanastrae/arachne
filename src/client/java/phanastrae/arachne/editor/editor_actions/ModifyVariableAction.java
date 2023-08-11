package phanastrae.arachne.editor.editor_actions;

import net.minecraft.text.Text;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.weave.element.GTV;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.List;
import java.util.function.BiConsumer;

public class ModifyVariableAction<E extends GTV, T> extends ModifiableAction {

    List<E> elements;
    List<T> oldValues;
    T newValue;
    BiConsumer<E, T> setter;

    public ModifyVariableAction(List<E> elements, List<T> oldValues, T newValue, BiConsumer<E, T> setter) {
        this.elements = elements;
        this.oldValues = oldValues;
        this.newValue = newValue;
        this.setter = setter;
    }

    public void update(List<E> elements, List<T> oldValues, T newValue) {
        if(!canEdit) {
            return;
        }
        // update elements and oldValues
        for(E e : this.elements) {
            e.setGTV(true);
        }
        for(int i = 0; i < elements.size(); i++) {
            E e = elements.get(i);
            if(!e.getGTV()) {
                T v = i < oldValues.size() ? oldValues.get(i) : null;
                this.elements.add(e);
                e.setGTV(true);
                this.oldValues.add(v);
            }
        }
        for(E e : this.elements) {
            e.setGTV(false);
        }
        // update new value
        this.newValue = newValue;
    }

    @Override
    public Text getTitle() {
        return Text.of("Modified Element" + (elements.isEmpty() ? "" : "s"));
    }

    @Override
    public void act(EditorInstance editorInstance) {
        for(E e : this.elements) {
            this.setter.accept(e, newValue);
        }
    }

    @Override
    public void undo(EditorInstance editorInstance) {
        for(int i = 0; i < this.elements.size(); i++) {
            if(i >= oldValues.size()) return;
            this.setter.accept(this.elements.get(i), this.oldValues.get(i));
        }
    }
}
