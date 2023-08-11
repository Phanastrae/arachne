package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.EditorSelectionManager;
import phanastrae.arachne.weave.SketchWeave;
import phanastrae.arachne.weave.element.sketch.SketchElement;
import phanastrae.arachne.weave.element.sketch.SketchTransform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SketchStructureViewWidget extends ScrollableWidget {

    EditorInstance editorInstance;
    TextRenderer textRenderer;

    boolean mod1held = false;
    boolean mod2held = false;

    public SketchStructureViewWidget(EditorInstance editorInstance, TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(x, y, width, height, text);
        this.editorInstance = editorInstance;
        this.textRenderer = textRenderer;
    }

    @Override
    protected int getContentsHeight() {
        return 4000;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 10;
    }

    @Override
    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack mat = context.getMatrices();
        mat.push();
        mat.translate(this.getX(), this.getY(), 0);
        SketchWeave sketchWeave = this.editorInstance.getSketchWeave();
        AtomicInteger x = new AtomicInteger();
        x.addAndGet(this.getPadding());
        AtomicInteger y = new AtomicInteger();
        y.addAndGet(this.getPadding());
        for(SketchTransform rb : sketchWeave.getRigidBodies()) {
            if(rb.parent == null) {
                drawTree(context, rb, x, y);
            }
        }
        mat.pop();
    }

    void drawTree(DrawContext context, SketchTransform rb, AtomicInteger x, AtomicInteger y) {
        drawElement(context, rb, x.get(), y.get());
        y.addAndGet(10);
        if(rb.children != null) {
            int i = 4;
            if(x.get() > this.width / 3) {
                i = 2;
                if(x.get() > this.width / 2) {
                    i = 1;
                }
            }
            x.addAndGet(i);
            int otherChildCount = 0;
            int selectedCount = 0;
            for (SketchElement child : rb.children) {
                if(child instanceof SketchTransform t) {
                    drawTree(context, t, x, y);
                } else {
                    otherChildCount++;
                    if(child.selected) {
                        selectedCount++;
                    }
                }
            }
            if(otherChildCount != 0) {
                drawCount(context, otherChildCount, selectedCount, x.get(), y.get());
                y.addAndGet(10);
            }
            x.addAndGet(-i);
        }
    }

    void drawElement(DrawContext context, SketchElement element, int x, int y) {
        if(y + 10 < this.getScrollY() || y > this.getHeight() + this.getScrollY() || x > this.getWidth()) return;
        context.drawText(this.textRenderer, "- " + element.getTypeName().getString(), x, y, element.selected ? 0xFFFFAF3F : (element.canDelete() ? 0xFFFFFFFF : 0xFFAFAFAF), false);
    }

    void drawCount(DrawContext context, int count, int selectedCount, int x, int y) {
        if(y + 10 < this.getScrollY() || y > this.getHeight() + this.getScrollY() || x > this.getWidth()) return;
        int color = selectedCount == 0 ? 0xFF9F9F9F : count == selectedCount ?  0xFFFFAF3F : 0xFFBFA76F;
        context.drawText(this.textRenderer, "- and " + count + " other children...", x, y, color, false);
    }

    @Override
    protected void drawBox(DrawContext context, int x, int y, int width, int height) {
        int i = this.isFocused() ? 0xFFFFFFFF : 0xFFAFAFAF;
        context.drawBorder(x, y, width, height, i);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF1F1F7F);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public boolean pickClickedTree(AtomicInteger i, SketchTransform rb) {
        if(i.get() < 0) return false;

        if(i.get() == 0) {
            EditorSelectionManager.SelectMode selectMode = mod1held ? (mod2held ? EditorSelectionManager.SelectMode.INTERSECT : EditorSelectionManager.SelectMode.ADD ) : (mod2held ? EditorSelectionManager.SelectMode.REMOVE : EditorSelectionManager.SelectMode.REPLACE);
            editorInstance.getSelectionManager().select(rb, selectMode);
            return true;
        } else {
            i.addAndGet(-1);
        }

        if(rb.children == null) return false;
        int otherChildCount = 0;
        for(SketchElement child : rb.children) {
            if(child instanceof SketchTransform t) {
                if (pickClickedTree(i, t)) {
                    return true;
                }
            } else {
                otherChildCount++;
            }
        }
        if(otherChildCount != 0) {
            if(pickOtherChildren(i, rb.children)) {
                return true;
            }
        }
        return false;
    }

    public boolean pickOtherChildren(AtomicInteger i, List<SketchElement> children) {
        if (i.get() < 0) return false;

        if (i.get() == 0) {
            EditorSelectionManager.SelectMode selectMode = mod1held ? (mod2held ? EditorSelectionManager.SelectMode.INTERSECT : EditorSelectionManager.SelectMode.ADD ) : (mod2held ? EditorSelectionManager.SelectMode.REMOVE : EditorSelectionManager.SelectMode.REPLACE);
            List<SketchElement> pick = new ArrayList<>();
            for(SketchElement child : children) {
                if(!(child instanceof SketchTransform)) {
                    pick.add(child);
                }
            }
            editorInstance.getSelectionManager().select(pick, selectMode);
            return true;
        } else {
            i.addAndGet(-1);
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isWithinBounds(mouseX, mouseY)) {
            double my = mouseY - this.getY() + this.getScrollY();
            AtomicInteger i = new AtomicInteger((int)(my/10));
            for(SketchTransform rb : this.editorInstance.getSketchWeave().getRigidBodies()) {
                if(rb.parent == null) {
                    if(pickClickedTree(i, rb)) {
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if(!focused) {
            this.mod1held = false;
            this.mod2held = false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            this.mod1held = true;
            return true;
        } else if(keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            this.mod2held = true;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            this.mod1held = false;
            return true;
        } else if(keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            this.mod2held = false;
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
