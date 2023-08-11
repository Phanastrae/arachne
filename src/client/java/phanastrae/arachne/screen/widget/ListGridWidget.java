package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.weave.element.sketch.SketchElement;
import phanastrae.arachne.weave.element.sketch.SketchPhysicsMaterial;
import phanastrae.arachne.weave.element.sketch.SketchRenderMaterial;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ListGridWidget extends ScrollableSubWindowWidget {

    Consumer<ListGridWidget> update = null;

    public ListGridWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
        super(textRenderer, x, y, width, height, message);
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 10;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // TODO
    }

    public void addButton(Text text, ButtonWidget.PressAction action, boolean active) {
        ButtonWidget w = ButtonWidget.builder(text, action).width(100).build();
        w.active = active;
        this.children.add(w);
    }

    public void clearChildren() {
        this.children.clear();
    }

    public void update() {
        if(update != null) {
            update.accept(this);
        }
    }

    public void setUpdate(Consumer<ListGridWidget> update) {
        this.update = update;
    }

    public static <T extends SketchElement>Consumer<ListGridWidget> getButtons(Supplier<List<T>> listSupplier, Function<T, Text> nameGetter, Consumer<T> onClick, Function<SketchElement, Boolean> active) {
        return (lgw) -> {
            List<T> list = listSupplier.get();
            List<T> listSorted = list.stream().sorted(Comparator.comparing((el) -> nameGetter.apply(el).getString())).toList();
            lgw.clearChildren();
            for (T element : listSorted) {
                lgw.addButton(nameGetter.apply(element), (b) -> onClick.accept(element), active.apply(element));
            }
            lgw.positionChildren();
        };
    }
}
