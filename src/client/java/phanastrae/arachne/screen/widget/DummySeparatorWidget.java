package phanastrae.arachne.screen.widget;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;

import java.util.function.Consumer;

public class DummySeparatorWidget implements Widget {

    int y = 0;

    public DummySeparatorWidget() {
    }

    @Override
    public void setX(int x) {
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }
}
