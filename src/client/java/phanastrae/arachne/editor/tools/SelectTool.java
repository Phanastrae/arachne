package phanastrae.arachne.editor.tools;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import oshi.util.tuples.Triplet;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.EditorSelectionManager;
import phanastrae.arachne.editor.RaycastResult;
import phanastrae.arachne.weave.element.sketch.SketchElement;

public class SelectTool extends BasicTool {
    public static final Identifier TEXTURE = Arachne.id("textures/gui/editor/editor_tools.png");
    public static final int U = 0;
    public static final int V = 0;

    boolean clicking = false;
    boolean drawingBox = false;
    double startX = 0;
    double startY = 0;

    @Override
    public Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public int getU() {
        return U;
    }

    @Override
    public int getV() {
        return V + 16 *((this.mod1held ? 1 : 0) + (this.mod2held ? 2 : 0) + (this.drawingBox ? 4 : 0));
    }

    @Override
    public String getId() {
        return "select";
    }

    @Override
    public void onSelect(EditorInstance editorInstance) {

    }

    @Override
    public void onDeselect(EditorInstance editorInstance) {
        if(this.drawingBox) {
            editorInstance.getSelectionManager().stopHighlight();
        }
        this.clicking = false;
        this.drawingBox = false;
        this.startX = 0;
        this.startY = 0;
        this.mod1held = false;
        this.mod2held = false;
        editorInstance.getSelectionManager().clearHighlight();
    }

    @Override
    public void tick(EditorInstance editorInstance, double mouseX, double mouseY) {
        if(this.clicking) {
            if (!this.drawingBox && Math.abs(this.startX - mouseX) > 2 || Math.abs(this.startY - mouseY) > 2) {
                editorInstance.getSelectionManager().startHighlight(startX, startY);
                drawingBox = true;
            }
            if (this.drawingBox) {
                editorInstance.getSelectionManager().updateHighlight(mouseX, mouseY);
            }
        }
        if(!this.drawingBox) {
            editorInstance.getSelectionManager().clearHighlight();
            RaycastResult hit = editorInstance.rayCast(mouseX, mouseY);
            if(hit != null) {
                editorInstance.getSelectionManager().highlight(hit.element());
            }
        }
    }

    @Override
    public boolean clickMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        this.clicking = true;
        this.drawingBox = false;
        this.startX = mouseX;
        this.startY = mouseY;
        return true;
    }

    @Override
    public boolean releaseMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        EditorSelectionManager.SelectMode selectMode;
        if(mod1held) {
            if(mod2held) {
                selectMode = EditorSelectionManager.SelectMode.INTERSECT;
            } else {
                selectMode = EditorSelectionManager.SelectMode.ADD;
            }
        } else {
            if(mod2held) {
                selectMode = EditorSelectionManager.SelectMode.REMOVE;
            } else {
                selectMode = EditorSelectionManager.SelectMode.REPLACE;
            }
        }
        this.clicking = false;
        if(this.drawingBox) {
            this.drawingBox = false;
            editorInstance.getSelectionManager().endHighlight(mouseX, mouseY, selectMode);
        } else {
            editorInstance.getSelectionManager().selectHighlight(selectMode);
        }
        return true;
    }

    @Override
    public void onSelectionChanged(EditorInstance editorInstance) {
    }

    @Override
    public void render(EditorInstance editorInstance, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {

    }
}
