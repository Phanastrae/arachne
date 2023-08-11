package phanastrae.arachne.old.property_handler;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class PropertyListWidget extends ScrollableWidget {

    private final List<Widget> children = new ArrayList<Widget>();
    private final TextRenderer textRenderer;
    @Nullable
    private Element focused;

    public PropertyListWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
    }

    @Override
    protected int getContentsHeight() {
        int h = (this.children.size() + 1) * this.getPadding();
        for(Widget w : this.children) {
            h += w.getHeight();
        }
        return h;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 10;
    }

    @Override
    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().translate(this.getX(), this.getY(), 0);
        context.getMatrices().translate(this.getPadding(), this.getPadding(), 0);
        mouseX -= this.getX() + this.getPadding();
        mouseY -= this.getY() + this.getPadding() - this.getScrollY();
        for(Widget child : this.children) {
            if(child instanceof Drawable d) {
                d.render(context, mouseX, mouseY, delta);
                context.getMatrices().translate(0, child.getHeight() + this.getPadding(), 0);
                mouseY -= child.getHeight() + this.getPadding();
            }
        }
        //context.drawText(this.textRenderer, "DEMO TEXT", 0, 0, 0xFFFFFFFF, true);
        context.getMatrices().pop();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // TODO
    }

    public void clearChildren() {
        this.children.clear();
    }

    public void addChild(Widget w) {
        this.children.add(w);
    }

    public boolean containsPoint(double mouseX, double mouseY) {
        return this.getX() <= mouseX && mouseX <= this.getX() + this.getWidth() && this.getY() <= mouseY && mouseY <= this.getY() + this.getHeight();
    }

    public int getInteriorWidth() {
        return Math.max(this.width - this.getPaddingDoubled(), 0);
    }

    public List<Widget> getChildren() {
        return this.children;
    }

    public void setFocused(@Nullable Element focused) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }
        if (focused != null) {
            focused.setFocused(true);
        }
        this.focused = focused;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!containsPoint(mouseX, mouseY)) {
            return false;
        }

        double mx = mouseX - (this.getX() + this.getPadding());
        double my = mouseY - (this.getY() + this.getPadding() - this.getScrollY());
        for(Widget child : this.children) {
            if(child instanceof Drawable d) {
                if(child instanceof Element e) {
                    if(e.mouseClicked(mx, my, button)) {
                        this.setFocused(e);
                        return true;
                    }
                }
                my -= child.getHeight() + this.getPadding();
            }
        }

        this.setFocused(null);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(this.focused != null) {
            if(this.focused.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if(this.focused instanceof TextPropertyWidget) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(this.focused != null) {
            if(this.focused.mouseScrolled(mouseX, mouseY, amount)) { // TODO: offset mouse pos properly
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void setFocused(boolean focused) {
        if(!focused) {
            this.setFocused(null);
        }
        super.setFocused(focused);
    }
}

