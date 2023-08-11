package phanastrae.arachne.editor.tools;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.RaycastResult;
import phanastrae.arachne.editor.editor_actions.CompositeAction;
import phanastrae.arachne.editor.editor_actions.EditorAction;
import phanastrae.arachne.editor.editor_actions.RemoveElementsAction;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class EraseTool extends BasicTool {
    public static final Identifier TEXTURE = Arachne.id("textures/gui/editor/editor_tools.png");
    public static final int U = 80;
    public static final int V = 0;

    boolean mouseHeld = false;
    double lastX = 0;
    double lastY = 0;
    boolean multiErase = false;

    @Nullable
    CompositeAction lastAction = null;

    @Override
    public @Nullable Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public int getU() {
        return U;
    }

    @Override
    public int getV() {
        return V;
    }

    @Override
    public String getId() {
        return "erase";
    }

    @Override
    public void onDeselect(EditorInstance editorInstance) {
        if(mouseHeld) {
            endDrag();
        }
        editorInstance.getSelectionManager().clearHighlight();
    }

    @Override
    public void tick(EditorInstance editorInstance, double mouseX, double mouseY) {
        editorInstance.getSelectionManager().clearHighlight();
        if(mouseHeld && !multiErase) {
            double dx = mouseX - this.lastX;
            double dy = mouseY - this.lastY;
            if(dx*dx+dy*dy>4) {
                multiErase = true;
            }
        }

        if(mouseHeld && multiErase) {
            boolean repeat = true;
            double STEPS = 8    ;
            double x = lastX;
            double dx = (mouseX - lastX) / STEPS;
            double y = lastY;
            double dy = (mouseY - lastY) / STEPS;
            for(int i = 0; i <= STEPS; i++) {
                int j = 0;
                while (j < 100 && repeat) {
                    j++;
                    repeat = false;
                    RaycastResult raycastResult = editorInstance.rayCast(x, y);
                    if (raycastResult != null) {
                        SketchElement element = raycastResult.element();
                        if(element != null && element.canDelete()) {
                            eraseElement(editorInstance, raycastResult.element());
                            repeat = true;
                        }
                    }
                }
                x += dx;
                y += dy;
            }
        } else if(!multiErase) {
            RaycastResult raycastResult = editorInstance.rayCast(mouseX, mouseY);
            if(raycastResult != null && raycastResult.element() != null) {
                editorInstance.getSelectionManager().highlight(raycastResult.element());
            }
        }

        if(multiErase) {
            lastX = mouseX;
            lastY = mouseY;
        }
    }

    @Override
    public boolean clickMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        editorInstance.getSelectionManager().clearSelection();
        if(!this.mouseHeld) {
            this.mouseHeld = true;
            this.lastX = mouseX;
            this.lastY = mouseY;
            RaycastResult raycastResult = editorInstance.rayCast(mouseX, mouseY);
            if(raycastResult != null && raycastResult.element() != null) {
                eraseElement(editorInstance, raycastResult.element());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean releaseMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        if(this.mouseHeld) {
            endDrag();
            return true;
        }
        return false;
    }

    void endDrag() {
        this.mouseHeld = false;
        this.lastX = 0;
        this.lastY = 0;
        this.multiErase = false;
        this.lastAction = null;
    }

    void eraseElement(EditorInstance editorInstance, SketchElement element) {
        if(!element.getAdded()) return;

        if(this.lastAction == null || !this.lastAction.canEdit()) {
            CompositeAction compositeAction = new CompositeAction(new ArrayList<>());
            compositeAction.setTitle(Text.of("Erased Element(s)"));
            editorInstance.doAction(compositeAction);
            this.lastAction = compositeAction;
        }
        EditorAction remove = RemoveElementsAction.of(List.of(element));
        AtomicReference<Boolean> bl = new AtomicReference<>(true);
        editorInstance.getActionQueue().updateLast(() -> {
            if(this.lastAction.canEdit()) {
                bl.set(this.lastAction.addAction(remove));
            }
        });
        if(!bl.get()) {
            editorInstance.doAction(remove);
        }
    }
}
