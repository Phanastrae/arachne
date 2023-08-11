package phanastrae.arachne.screen.widget;

import net.minecraft.client.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.ArrayList;
import java.util.List;

public interface PropertyWidget<E> extends Widget, Tickable {
    void setList(@Nullable List<E> list);
    String getName();
    void setWidth(int width);

    default void setElement(@Nullable E element) {
        List<E> l = new ArrayList<>();
        this.setList(l);
        if(element != null) {
            l.add(element);
        }
    }
}
