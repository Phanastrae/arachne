package phanastrae.arachne.editor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.editor.editor_actions.*;
import phanastrae.arachne.editor.editor_tabs.EditorTab;
import phanastrae.arachne.editor.tools.EditorTool;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.util.Line;
import phanastrae.arachne.util.SketchUtil;
import phanastrae.arachne.weave.SketchWeave;
import phanastrae.arachne.weave.element.sketch.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class EditorInstance {

    EditorMainScreen screen;
    SketchWeave sketchWeave;
    EditorActionQueue editorActionQueue = new EditorActionQueue(this);
    EditorToolHandler editorToolHandler = new EditorToolHandler(this);
    EditorSelectionManager selectionManager = new EditorSelectionManager(this);
    EditorTabHandler editorTabHandler = new EditorTabHandler(this);

    public EditorInstance(EditorMainScreen screen, SketchWeave sketchWeave) {
        this.screen = screen;
        if(sketchWeave == null) {
            this.sketchWeave = new SketchWeave();
        } else {
            this.sketchWeave = sketchWeave;
        }
    }

    public EditorMainScreen getScreen() {
        return this.screen;
    }

    public EditorSelectionManager getSelectionManager() {
        return this.selectionManager;
    }

    public EditorTabHandler getTabHandler() {
        return this.editorTabHandler;
    }

    public Collection<EditorTab> getTabs() {
        return this.editorTabHandler.getTabs();
    }

    public void render(float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        this.editorToolHandler.render(tickDelta, matrices, vertexConsumers);
    }

    @Nullable
    public RaycastResult rayCast(double mouseX, double mouseY) {
        Line mouseRay = this.getMouseRay(this.screen.mouseXtoScreenSpace(mouseX), this.screen.mouseYtoScreenSpace(mouseY));
        return this.rayCast(mouseRay);
    }

    @Nullable
    public RaycastResult rayCast(Line rayLocal) {
        SketchElement nearestElement = null;
        Vec3d hitPos = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for(SketchVertex node : this.sketchWeave.getNodes()) {
            Pair<Vec3d, Double> hit = node.getRayHit(rayLocal);
            if(hit == null) continue;
            if(hit.getRight() < minDistance) {
                nearestElement = node;
                hitPos = hit.getLeft();
                minDistance = hit.getRight();
            }
        }
        for(SketchEdge edge : this.sketchWeave.getEdges()) {
            Pair<Vec3d, Double> hit = edge.getRayHit(rayLocal);
            if(hit == null) continue;
            if(hit.getRight() < minDistance) {
                nearestElement = edge;
                hitPos = hit.getLeft();
                minDistance = hit.getRight();
            }
        }
        for(SketchFace face : this.sketchWeave.getFaces()) {
            Pair<Vec3d, Double> hit = face.getRayHit(rayLocal);
            if(hit == null) continue;
            if(hit.getRight() < minDistance) {
                nearestElement = face;
                hitPos = hit.getLeft();
                minDistance = hit.getRight();
            }
        }
        if(nearestElement == null) return null;
        return new RaycastResult(nearestElement, hitPos, minDistance);
    }

    public Line getMouseRay(double mouseXScreenSpace, double mouseYScreenSpace) {
        double fov = CameraController.getInstance().getFOV(70);
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        MatrixStack matrices1 = new MatrixStack();
        matrices1.multiplyPositionMatrix(MinecraftClient.getInstance().gameRenderer.getBasicProjectionMatrix(fov));
        Matrix4f projectionMatrix = matrices1.peek().getPositionMatrix();

        MatrixStack matrices2 = new MatrixStack();
        matrices2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices2.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        Vec3d originPos = this.screen.getOriginPos();
        matrices2.translate(originPos.x, originPos.y, originPos.z);
        Matrix4f viewMatrix = matrices2.peek().getPositionMatrix();


        Vector4f mousePos = new Vector4f((float)mouseXScreenSpace, (float)mouseYScreenSpace, 0, 1);
        Vector4f projPos = new Vector4f((float)mouseXScreenSpace, (float)mouseYScreenSpace, 1, 1);
        projectionMatrix = projectionMatrix.invert();
        mousePos = mousePos.mul(projectionMatrix);
        projPos = projPos.mul(projectionMatrix);
        if(mousePos.w != 0) {
            mousePos = mousePos.div(mousePos.w);
        }
        if(projPos.w != 0) {
            projPos = projPos.div(projPos.w);
        }
        viewMatrix = viewMatrix.invert();
        mousePos = mousePos.mul(viewMatrix);
        projPos = projPos.mul(viewMatrix);
        Vec3d mousePosProjected = new Vec3d(mousePos.x, mousePos.y, mousePos.z);
        Vec3d mouseLookProjected = new Vec3d(projPos.x - mousePos.x, projPos.y - mousePos.y, projPos.z - mousePos.z);
        mouseLookProjected = mouseLookProjected.normalize();
        return new Line(mousePosProjected, mouseLookProjected);
    }

    public Line getMouseRayWorld(Line mouseRayLocal) {
        return new Line(mouseRayLocal.point.add(this.screen.getOriginPos()), mouseRayLocal.offset);
    }

    public void tick(double mouseX, double mouseY) {
        this.editorToolHandler.tick(mouseX, mouseY);
    }

    public SketchWeave getSketchWeave() {
        return this.sketchWeave;
    }

    public EditorActionQueue getActionQueue() {
        return this.editorActionQueue;
    }

    public EditorToolHandler getEditorToolHandler() {
        return this.editorToolHandler;
    }

    public void doAction(EditorAction action) {
        this.editorActionQueue.doNewAction(action);
    }

    public boolean undo() {
        return this.editorActionQueue.undo();
    }

    public boolean redo() {
        return this.editorActionQueue.redo();
    }

    public void deleteSelected() {
        ArrayList<SketchElement> selected = (ArrayList<SketchElement>)this.getSelectionManager().selected.clone();
        if(selected.isEmpty()) return;
        EditorAction remove = RemoveElementsAction.of(selected);
        if(remove != null) {
            this.doAction(remove);
        }
    }

    public void addVertex() {
        SketchTransform parent = null;
        if(getSelectionManager().getSelection().size() == 1) {
            SketchElement e = getSelectionManager().getSelection().get(0);
            if(e instanceof SketchTransform t) {
                parent = t;
            }
        }
        if(parent == null) {
            parent = SketchUtil.getCommonParent(getSelectionManager().getSelection());
        }
        if(parent == null) {
            parent = sketchWeave.getRoot();
        }
        SketchVertexCollection vc = this.getOrCreateVertexCollection(parent);
        SketchVertex node = new SketchVertex(vc);
        Vec3d worldPos = this.screen.globalToLocal(CameraController.getInstance().targetPos);
        worldPos = vc.getLocalCoords(worldPos);
        node.setPos(worldPos);
        EditorAction addAction = new AddElementsAction(List.of(node));
        EditorAction selAction = new ChangeSelectionAction(List.of(node), (List<SketchElement>)this.selectionManager.getSelection().clone());
        this.doAction(new CompositeAction(List.of(addAction, selAction), Text.of("Add Node")));
    }

    public void addObject(Function<SketchTransform, SketchTransform> create) {
        SketchTransform parent;
        if(getSelectionManager().getSelection().size() == 1 && getSelectionManager().getSelection().get(0) instanceof SketchTransform t) {
            parent = t;
        } else {
            parent = this.getSketchWeave().getRoot();
        }
        SketchTransform t = create.apply(parent);
        Vec3d worldPos = this.screen.globalToLocal(CameraController.getInstance().targetPos);
        worldPos = t.getLocalCoords(worldPos);
        t.setPosition(worldPos);
        EditorAction addAction = new AddElementsAction(List.of(t));
        EditorAction selAction = new ChangeSelectionAction(List.of(t), (List<SketchElement>)this.selectionManager.getSelection().clone());
        this.doAction(new CompositeAction(List.of(addAction, selAction), Text.of("Add " + t.getTypeName().getString())));
    }

    public void addTransform() {
        addObject(SketchTransform::new);
    }

    public void addVertexCollection() {
        addObject(SketchVertexCollection::new);
    }

    public void addPhysicsMaterial() {
        SketchPhysicsMaterial mat = new SketchPhysicsMaterial(Text.translatable("arachne.editor.type.physicsMaterial").getString());
        this.doAction(new AddElementsAction(List.of(mat)));
    }
    public void addRenderMaterial() {
        SketchRenderMaterial mat = new SketchRenderMaterial(Text.translatable("arachne.editor.type.renderMaterial").getString());
        this.doAction(new AddElementsAction(List.of(mat)));
    }

    public void onSelectionChanged() {
        this.editorToolHandler.onSelectionChanged();
        this.screen.onSelectionChanged();
    }

    public SketchVertexCollection getOrCreateVertexCollection(SketchTransform t) {
        if(t instanceof SketchVertexCollection svc) {
            return svc;
        }

        List<SketchElement> children = t.getChildren();
        if(children != null) {
            for (SketchElement e : children) {
                if(e instanceof SketchVertexCollection vc) {
                    return vc;
                }
            }
        }

        SketchVertexCollection vc = new SketchVertexCollection(t);
        this.doAction(new AddElementsAction(List.of(vc)));
        return vc;
    }

    @Nullable
    public EditorTool getTool() {
        return this.editorToolHandler.getActiveTool();
    }
}
