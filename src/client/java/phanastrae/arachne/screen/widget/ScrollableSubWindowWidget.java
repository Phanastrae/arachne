package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import phanastrae.arachne.Arachne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ScrollableSubWindowWidget extends ScrollableWidget implements Tickable {

    final TextRenderer textRenderer;
    final List<Widget> children;
    @Nullable
    private Element focused;
    int contentsHeight = this.getPaddingDoubled();

    public ScrollableSubWindowWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
        this.children = new ArrayList<>();
    }

    public void addChildren(Collection<? extends Widget> w) {
        this.children.addAll(w);
    }

    public void addChild(Widget w) {
        this.children.add(w);
    }

    public void addLine() {
        this.children.add(new DummyNewLineWidget());
    }

    public void addSeparator() {
        this.children.add(new DummySeparatorWidget());
        addLine();
    }

    public void clearChildren() {
        this.children.clear();
    }

    public void positionChildren() {
        int x = 0;
        int y = 0;
        int maxHeight = 0;
        for(Widget w : this.children) {
            if(w instanceof DummyNewLineWidget) {
                x = 0;
                y += maxHeight + getPadding();
                maxHeight = 0;
            } else {
                if(x != 0 && x + w.getWidth() + getPadding() >= width) {
                    x = 0;
                    y += maxHeight + getPadding();
                    maxHeight = 0;
                }
                w.setPosition(w.getX() + x, w.getY() + y);
                x += w.getWidth() + getPadding();
                if(w.getHeight() > maxHeight) {
                    maxHeight = w.getHeight();
                }
            }
        }
        this.contentsHeight = y + maxHeight + getPadding();
    }

    public int getInteriorWidth() {
        return Math.max(this.width - this.getPaddingDoubled(), 0);
    }

    public void setFocused(@Nullable Element focused) {
        if(this.focused == focused) return;

        if (this.focused != null) {
            this.focused.setFocused(false);
        }
        if (focused != null) {
            focused.setFocused(true);
        }

        this.focused = focused;

        if(focused != null && !focused.isFocused()) {
            this.focused.setFocused(false);
            this.focused = null;
        }
    }

    @Nullable
    public Element getFocusedChild() {
        return this.focused;
    }

    @Override
    public void tick() {
        for(Widget w : this.children) {
            if(w instanceof Tickable t) {
                t.tick();
            }
        }
    }

    @Override
    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().translate(this.getX(), this.getY(), 0);
        context.getMatrices().translate(this.getPadding(), this.getPadding(), 0);
        mouseX -= this.getX() + this.getPadding();
        mouseY -= this.getY() + this.getPadding() - this.getScrollY();
        int i = this.isFocused() ? 0xFFFFFFFF : 0xFFAFAFAF;
        for(Widget w : this.children) {
            // TODO cull OOB widgets

            if(w instanceof Drawable d) {
                d.render(context, mouseX, mouseY, delta);
            }
            if(w instanceof DummySeparatorWidget) {
                context.drawHorizontalLine(-this.getPadding(), this.width-this.getPadding(), w.getY(), i);
            }
        }
        context.getMatrices().pop();
    }

    @Override
    protected void drawBox(DrawContext context, int x, int y, int width, int height) {
        int i = this.isFocused() ? 0xFFFFFFFF : 0xFFAFAFAF;
        context.drawBorder(x, y, width, height, i);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xAF3F3FAF);
    }

    @Override
    protected int getContentsHeight() {
        return this.contentsHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!isWithinBounds(mouseX, mouseY)) {
            return false;
        }

        double mx = mouseX - (this.getX() + this.getPadding());
        double my = mouseY - (this.getY() + this.getPadding() - this.getScrollY());
        for(Widget w : this.children) {
            if(w instanceof Element e) {
                if(e.mouseClicked(mx, my, button)) {
                    this.setFocused(e);
                    return true;
                }
            }
        }

        this.setFocused(null);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(this.focused != null) {
            if(this.focused.mouseScrolled(mouseX, mouseY, amount)) { // TODO: offset mouse pos properly
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(this.focused != null) {
            if(this.focused.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if(this.focused instanceof GenericPropertyWidget) {
                if(keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                    this.setFocused(null);
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(this.focused != null) {
            if(this.focused.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(this.focused != null) {
            if(this.focused.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
        if(this.isFocused() == focused) return;

        if(!focused) {
            this.setFocused(null);
        }
        super.setFocused(focused);
    }
}
