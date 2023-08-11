package phanastrae.arachne.editor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.editor.editor_actions.ChangeSelectionAction;
import phanastrae.arachne.weave.element.sketch.SketchEdge;
import phanastrae.arachne.weave.element.sketch.SketchElement;
import phanastrae.arachne.weave.element.sketch.SketchVertex;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditorSelectionManager {

    EditorInstance editorInstance;

    ArrayList<SketchElement> highlighted = new ArrayList<>();
    ArrayList<SketchElement> selected = new ArrayList<>();

    boolean highlighting = false;
    double highlightStartX = 0;
    double highlightStartY = 0;

    public EditorSelectionManager(EditorInstance editorInstance) {
        this.editorInstance = editorInstance;
    }

    public enum SelectMode {
        REPLACE,
        ADD,
        REMOVE,
        INTERSECT
    }

    public void draw(DrawContext context, int mouseX, int mouseY) {
        if(this.highlighting) {
            int minX = (int) Math.min(highlightStartX, mouseX);
            int maxX = (int) Math.max(highlightStartX, mouseX);
            int minY = (int) Math.min(highlightStartY, mouseY);
            int maxY = (int) Math.max(highlightStartY, mouseY);

            context.drawBorder(minX, minY, maxX - minX, maxY - minY, 0xFFFFFFFF);
            context.fill(minX, minY, maxX, maxY, 0x3FFFFFFF);
        }
    }

    public void startHighlight(double mouseX, double mouseY) {
        if(this.highlighting) {
            clearHighlight();
        }
        this.highlighting = true;
        this.highlightStartX = mouseX;
        this.highlightStartY = mouseY;
    }

    public void updateHighlight(double mouseX, double mouseY) {
        if(!this.highlighting) {
            return;
        }

        double minX = Math.min(highlightStartX, mouseX);
        double maxX = Math.max(highlightStartX, mouseX);
        double minY = Math.min(highlightStartY, mouseY);
        double maxY = Math.max(highlightStartY, mouseY);
        double minXss = this.editorInstance.screen.mouseXtoScreenSpace(minX);
        double maxXss = this.editorInstance.screen.mouseXtoScreenSpace(maxX);
        double minYss = this.editorInstance.screen.mouseYtoScreenSpace(maxY);
        double maxYss = this.editorInstance.screen.mouseYtoScreenSpace(minY);

        this.clearHighlight();
        double fov = CameraController.getInstance().getFOV(70);
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        MatrixStack matrices1 = new MatrixStack();
        matrices1.multiplyPositionMatrix(MinecraftClient.getInstance().gameRenderer.getBasicProjectionMatrix(fov));
        Matrix4f projectionMatrix = matrices1.peek().getPositionMatrix();

        MatrixStack matrices2 = new MatrixStack();
        matrices2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices2.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        Vec3d originPos = this.editorInstance.screen.getOriginPos();
        matrices2.translate(originPos.x, originPos.y, originPos.z);
        Matrix4f viewMatrix = matrices2.peek().getPositionMatrix();

        for(SketchVertex node : editorInstance.sketchWeave.getNodes()) {
            Vector4f v = new Vector4f(node.gx, node.gy, node.gz, 1);
            v = v.mul(viewMatrix);
            v = v.mulProject(projectionMatrix);
            if(minXss <= v.x && v.x <= maxXss && minYss <= v.y && v.y <= maxYss && v.z < 1) {
                this.highlight(node);
            }
        }
        for(SketchEdge edge : editorInstance.sketchWeave.getEdges()) {
            Vector4f v1 = new Vector4f(edge.start.gx, edge.start.gy, edge.start.gz, 1);
            Vector4f v2 = new Vector4f(edge.end.gx, edge.end.gy, edge.end.gz, 1);
            v1 = v1.mul(viewMatrix);
            v1 = v1.mulProject(projectionMatrix);
            v2 = v2.mul(viewMatrix);
            v2 = v2.mulProject(projectionMatrix);

            if(minXss <= v1.x && v1.x <= maxXss && minYss <= v1.y && v1.y <= maxYss && v1.z < 1) {
                if(minXss <= v2.x && v2.x <= maxXss && minYss <= v2.y && v2.y <= maxYss && v2.z < 1) {
                    this.highlight(edge);
                }
            }
        }
    }

    public void endHighlight(double mouseX, double mouseY, SelectMode selectMode) {
        if(!this.highlighting) {
            this.clearHighlight();
            return;
        }
        this.updateHighlight(mouseX, mouseY);
        this.selectHighlight(selectMode);
    }

    public void selectHighlight(SelectMode selectMode) {
        this.select(this.highlighted, selectMode);
        this.stopHighlight();
    }

    public void stopHighlight() {
        this.clearHighlight();
        this.highlighting = false;
    }

    public void clearHighlight() {
        for(SketchElement element : this.highlighted) {
            element.highlighted = false;
        }
        this.highlighted.clear();
    }

    public void clearSelection() {
        this.select(List.of(), SelectMode.REPLACE);
    }

    public void highlight(SketchElement element) {
        if(!element.highlighted) {
            element.highlighted = true;
            this.highlighted.add(element);
        }
    }

    public void select(SketchElement element, SelectMode selectMode) {
        ArrayList<SketchElement> list = new ArrayList<>();
        list.add(element);
        this.select(list, selectMode);
    }

    public void select(List<SketchElement> elements, SelectMode selectMode) {
        ArrayList<SketchElement> removeList = new ArrayList<>();
        if(selectMode != SelectMode.ADD) {
            for (SketchElement element : this.selected) {
                boolean inNew = elements.contains(element);
                if(selectMode == SelectMode.REMOVE) {
                    if(inNew) {
                        removeList.add(element);
                    }
                } else if(selectMode == SelectMode.REPLACE || selectMode == SelectMode.INTERSECT) {
                    if(!inNew) {
                        removeList.add(element);
                    }
                }
            }
        }

        ArrayList<SketchElement> addList = new ArrayList<>();
        if(selectMode != SelectMode.REMOVE && selectMode != SelectMode.INTERSECT) {
            for (SketchElement element : elements) {
                boolean inOld = element.selected;
                if(!inOld) {
                    addList.add(element);
                }
            }
        }
        if(addList.isEmpty()) addList = null;
        if(removeList.isEmpty()) removeList = null;

        if(addList != null || removeList != null) {
            this.editorInstance.doAction(new ChangeSelectionAction(addList, removeList));
        }
    }

    public void highlight(List<SketchElement> elements) {
        for(SketchElement element : elements) {
            this.highlight(element);
        }
    }

    public void unselectIf(Function<SketchElement, Boolean> func) {
        ArrayList<SketchElement> remove = new ArrayList<>();
        for(SketchElement element : this.selected) {
            if(func.apply(element)) {
                remove.add(element);
            }
        }
        this.select(remove, SelectMode.REMOVE);
    }

    public boolean hasSelection() {
        return !this.selected.isEmpty();
    }

    public boolean hasHighlight() {
        return !this.highlighted.isEmpty();
    }

    public void forEachHighlighted(Consumer<SketchElement> action) {
        this.highlighted.forEach(action);
    }

    public void forEachSelected(Consumer<SketchElement> action) {
        this.selected.forEach(action);
    }

    public ArrayList<SketchElement> getHighlight() {
        return this.highlighted;
    }
    public ArrayList<SketchElement> getSelection() {
        return this.selected;
    }

    public void selectAll() {
        ArrayList<SketchElement> list = new ArrayList<>();
        for(SketchElement element : this.editorInstance.sketchWeave.getNodes()) {
            if(!element.selected) {
                list.add(element);
            }
        }
        for(SketchElement element : this.editorInstance.sketchWeave.getEdges()) {
            if(!element.selected) {
                list.add(element);
            }
        }
        for(SketchElement element : this.editorInstance.sketchWeave.getFaces()) {
            if(!element.selected) {
                list.add(element);
            }
        }
        for(SketchElement element : this.editorInstance.sketchWeave.getRigidBodies()) {
            if(!element.selected) {
                //list.add(element);
            }
        }
        for(SketchElement element : this.editorInstance.sketchWeave.getVertexCollections()) {
            if(!element.selected) {
                //list.add(element);
            }
        }
        if(!list.isEmpty()) {
            this.editorInstance.doAction(new ChangeSelectionAction(list, null));
        }
    }
}
