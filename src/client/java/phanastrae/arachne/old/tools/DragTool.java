package phanastrae.arachne.old.tools;


import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.old.EditorMainScreen;
import phanastrae.old.Node;
import phanastrae.arachne.util.CenteredPlane;
import phanastrae.arachne.util.Line;

public class DragTool implements ToolType {

    @Nullable
    Vec3d lastCameraPos = null;

    @Nullable
    public Line lastMouseRay = null;

    @Override
    public void onSwitchTo(EditorMainScreen mls) {
        this.lastCameraPos = null;
        this.lastMouseRay = null;
    }

    @Override
    public void onTick(EditorMainScreen mls) {
        if(this.lastMouseRay == null) return;
        if(mls.selectionMode == EditorMainScreen.SelectionMode.VERTEX && !mls.selection.getNodes().isEmpty()) {
            Vec3d newCameraPos = CameraController.getInstance().targetPos;
            this.lastCameraPos = newCameraPos;
            Vec3d lastToNew = null;
            if (lastCameraPos != null) {
                lastToNew = newCameraPos.subtract(lastCameraPos);
                for (Node node : mls.selection.getNodes()) { // TODO: tweak
                    // if camera target has moved, move node
                    if(lastToNew != null) {
                        node.pos = node.pos.add(lastToNew);
                    }
                }

                if(this.lastMouseRay != null) {
                    this.lastMouseRay = new Line(lastMouseRay.point.add(lastToNew), lastMouseRay.offset);
                }
            }

            // move around center of mass //TODO is this good
            Vec3d center = new Vec3d(0, 0, 0);
            double totalMass = 0;
            for(Node node : mls.selection.nodes) {
                center = center.add(node.pos.multiply(node.mass));
                totalMass += node.mass;
            }
            if(totalMass != 0) {
                center = center.multiply(1/totalMass);
            } else {
                center = new Vec3d(0, 0, 0);
                for(Node node : mls.selection.nodes) {
                    center = center.add(node.pos);
                }
                center = center.multiply(1.0/mls.selection.nodes.size());
            }

            if(this.lastMouseRay != null) {
                Vec3d cameraLook = CameraController.getCameraLookVector(MinecraftClient.getInstance().gameRenderer.getCamera());
                //TODO: add other modes? ie maintain x, maintain y, maintain z
                CenteredPlane plane = new CenteredPlane(center, cameraLook);
                Vec3d newPoint = plane.intersectLine(mls.mouseRay, 1 / 64f);
                Vec3d oldPoint = plane.intersectLine(this.lastMouseRay, 1 / 64f);
                if (newPoint != null && oldPoint != null) {
                    Vec3d dif = newPoint.subtract(oldPoint);
                    for(Node node : mls.selection.nodes) {
                        node.pos = node.pos.add(dif);
                        node.clearVelocity();
                    }
                }
            }
        }
        /*
        if(mls.selectionMode == EditorMainScreen.SelectionMode.FACE && !mls.selection.getFaces().isEmpty()) {
            // TODO: fix functionality for multiple faces at once
            // TODO: make not terrible
            for(Face face : mls.selection.getFaces()) {
                Vec3d faceMid = face.getCenterPos();
                Vec3d faceNormal = face.getNormal();
                Line normalLine = new Line(faceMid, faceNormal);
                Line mouseRay = mls.mouseRay;
                Vec3d newMid = normalLine.findNearestPointToLine(mouseRay);
                if(newMid != null) {
                    Vec3d offset = newMid.subtract(faceMid);
                    for (Node n : face.nodes) {
                        n.pos = n.pos.add(offset);
                    }
                }
            }
        }
        */
        this.lastMouseRay = mls.mouseRay;
    }

    @Override
    public void onClick(EditorMainScreen mls) {
        this.lastMouseRay = null;
        if(mls.selection.getNodes().isEmpty() && mls.highlightedNode != null) {
            mls.selection.addNode(mls.highlightedNode);
        }
        if(mls.selection != null) {
            this.lastMouseRay = mls.mouseRay;
        }
    }

    @Override
    public void onRelease(EditorMainScreen mls) {
        this.lastCameraPos = null;
        this.lastMouseRay = null;
    }

    @Override
    public String getId() {
        return "drag";
    }
}