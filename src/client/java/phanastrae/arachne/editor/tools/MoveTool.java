package phanastrae.arachne.editor.tools;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.editor_actions.CompositeAction;
import phanastrae.arachne.editor.editor_actions.ModifiableAction;
import phanastrae.arachne.editor.editor_actions.ModifyVariablesAction;
import phanastrae.arachne.util.CenteredPlane;
import phanastrae.arachne.util.Line;
import phanastrae.arachne.weave.element.Positionable;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.ArrayList;
import java.util.List;

public class MoveTool extends BasicTool {
    public static final Identifier TEXTURE = Arachne.id("textures/gui/editor/editor_tools.png");
    public static final int U = 64;
    public static final int V = 0;

    @Nullable
    CompositeAction moveAction = null;
    @Nullable
    List<Double> newX = null;
    @Nullable
    List<Double> newY = null;
    @Nullable
    List<Double> newZ = null;

    Vec3d lastMousePos = null;

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
        return V;
    }

    @Override
    public String getId() {
        return "move";
    }

    @Override
    public void onSelect(EditorInstance editorInstance) {
    }

    @Override
    public void onDeselect(EditorInstance editorInstance) {
        finalise(editorInstance);
    }

    @Override
    public void tick(EditorInstance editorInstance, double mouseX, double mouseY) {
        if(moveAction != null && moveAction.canEdit()) {;
            Vec3d cameraLook = CameraController.getCameraLookVector(MinecraftClient.getInstance().gameRenderer.getCamera());
            CenteredPlane plane = new CenteredPlane(editorInstance.getScreen().globalToLocal(CameraController.getInstance().targetPos), cameraLook);

            Line newMouseRay = editorInstance.getMouseRay(editorInstance.getScreen().mouseXtoScreenSpace(mouseX), editorInstance.getScreen().mouseYtoScreenSpace(mouseY));

            Vec3d newMousePos = plane.intersectLine(newMouseRay, 0);
            if(newMousePos == null) return;

            if(this.lastMousePos == null) {
                this.lastMousePos = newMousePos;
                return;
            }

            Vec3d dif = newMousePos.subtract(this.lastMousePos);
            this.lastMousePos = newMousePos;

            editorInstance.getActionQueue().updateLast(() -> {
                if(newX == null || newY == null || newZ == null) return;
                for (int i = 0; i < newX.size(); i++) {
                    newX.set(i, newX.get(i) + dif.x);
                }
                for (int i = 0; i < newY.size(); i++) {
                    newY.set(i, newY.get(i) + dif.y);
                }
                for (int i = 0; i < newZ.size(); i++) {
                    newZ.set(i, newZ.get(i) + dif.z);
                }
            });
        } else {
            lastMousePos = null;
        }
    }

    @Override
    public boolean clickMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        if(this.moveAction != null && !this.moveAction.canEdit()) {
            finalise(editorInstance);
        }

        if(this.moveAction == null) {
            List<Positionable> list = new ArrayList<>();
            for(SketchElement e : editorInstance.getSelectionManager().getSelection()) {
                if(e instanceof Positionable p) {
                    list.add(p);
                }
            }
            if(list.isEmpty()) return false;

            ArrayList<Double> oldX = new ArrayList<>(list.size());
            ArrayList<Double> oldY = new ArrayList<>(list.size());
            ArrayList<Double> oldZ = new ArrayList<>(list.size());
            this.newX = new ArrayList<>(list.size());
            this.newY = new ArrayList<>(list.size());
            this.newZ = new ArrayList<>(list.size());
            for(Positionable p : list) {
                oldX.add(p.getX());
                oldY.add(p.getY());
                oldZ.add(p.getZ());
                newX.add(p.getX());
                newY.add(p.getY());
                newZ.add(p.getZ());
            }

            ModifyVariablesAction<? extends Positionable, Double> modX = new ModifyVariablesAction<>(list, oldX, this.newX, Positionable::setX);
            ModifyVariablesAction<? extends Positionable, Double> modY = new ModifyVariablesAction<>(list, oldY, this.newY, Positionable::setY);
            ModifyVariablesAction<? extends Positionable, Double> modZ = new ModifyVariablesAction<>(list, oldZ, this.newZ, Positionable::setZ);
            CompositeAction moveAction = new CompositeAction(List.of(modX, modY, modZ), Text.of("Moved " + list.size() + " Object(s)"));
            editorInstance.doAction(moveAction);
            this.moveAction = moveAction;
        } else {
            finalise(editorInstance);
        }
        return true;
    }

    @Override
    public boolean releaseMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        return false;
    }

    @Override
    public boolean pressKey(EditorInstance editorInstance, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean releaseKey(EditorInstance editorInstance, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public void onSelectionChanged(EditorInstance editorInstance) {
        finalise(editorInstance);
    }

    @Override
    public void render(EditorInstance editorInstance, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
    }

    public void finalise(EditorInstance editorInstance) {
        if(this.moveAction != null) {
            ModifiableAction action = this.moveAction;
            this.moveAction = null;
            this.newX = null;
            this.newY = null;
            this.newZ = null;
            this.lastMousePos = null;

            if(action.canEdit()) {
                editorInstance.getActionQueue().finaliseCurrentAction();
            }
        }
    }
}
