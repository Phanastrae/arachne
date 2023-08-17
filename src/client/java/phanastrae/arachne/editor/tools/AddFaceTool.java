package phanastrae.arachne.editor.tools;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.render.ModRenderLayers;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.EditorSelectionManager;
import phanastrae.arachne.editor.RaycastResult;
import phanastrae.arachne.editor.editor_actions.AddElementsAction;
import phanastrae.arachne.render.PosColorBufferBuilder;
import phanastrae.arachne.screen.widget.ToolSettingsWidget;
import phanastrae.arachne.weave.WeaveRenderer;
import phanastrae.arachne.weave.element.sketch.SketchEdge;
import phanastrae.arachne.weave.element.sketch.SketchElement;
import phanastrae.arachne.weave.element.sketch.SketchFace;
import phanastrae.arachne.weave.element.sketch.SketchVertex;

import java.util.ArrayList;
import java.util.List;

public class AddFaceTool extends BasicTool {
    public static final Identifier TEXTURE = Arachne.id("textures/gui/editor/editor_tools.png");
    public static final int U = 32;
    public static final int V = 0;

    boolean doubleSided = false;
    boolean flipSides = false;

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
        return "addFace";
    }

    boolean makingFace = false;
    ArrayList<SketchVertex> faceVertices = new ArrayList<>();
    @Nullable
    SketchFace face = null;
    boolean showFace = false;

    @Override
    public void onSelect(EditorInstance editorInstance) {
    }

    @Override
    public void onDeselect(EditorInstance editorInstance) {
        stopFace(editorInstance);
        editorInstance.getSelectionManager().clearHighlight();
    }

    @Override
    public void tick(EditorInstance editorInstance, double mouseX, double mouseY) {
        this.showFace = false;
        editorInstance.getSelectionManager().clearHighlight();
        SketchVertex last = this.faceVertices.isEmpty() ? null : this.faceVertices.get(this.faceVertices.size() - 1);
        if(this.makingFace && !this.faceVertices.isEmpty()) {
            List<SketchEdge> c = last.getChildren();
            if(c != null) {
                for(SketchEdge edge : c) {
                    if(edge.start == last || (this.faceVertices.get(0) == edge.start && this.faceVertices.size() >= 3) || !this.faceVertices.contains(edge.start)) {
                        if(edge.end == last || (this.faceVertices.get(0) == edge.end && this.faceVertices.size() >= 3) || !this.faceVertices.contains(edge.end)) {
                            editorInstance.getSelectionManager().highlight(edge);
                        }
                    }
                }
            }
        }
        RaycastResult hit = editorInstance.rayCast(mouseX, mouseY);
        SketchElement h = hit == null ? null : hit.element();
        if(h instanceof SketchEdge e) {
            if(e.start == last) {
                h = e.end;
            } else if(e.end == last) {
                h = e.start;
            }
        }
        if(h instanceof SketchVertex n) {
            boolean highlight = false;
            if(this.makingFace) {
                if(!this.faceVertices.isEmpty() && last != null) {
                    if (!this.faceVertices.contains(n)) {
                        SketchEdge edge = last.getConnection(n);
                        if (edge != null) {
                            highlight = true;
                        }
                    } else if(this.faceVertices.get(0) == n && this.faceVertices.size() >= 3) {
                        highlight = true;
                        this.showFace = true;
                    }
                }
            } else {
                highlight = true;
            }
            if(highlight) {
                editorInstance.getSelectionManager().highlight(n);
            }
        }

        if(this.face != null) {
            this.swapIfNeeded(this.face);
        }
    }

    @Override
    public boolean clickMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        RaycastResult hit = editorInstance.rayCast(mouseX, mouseY);
        SketchVertex last = this.faceVertices.isEmpty() ? null : this.faceVertices.get(this.faceVertices.size() - 1);
        SketchElement h = hit == null ? null : hit.element();
        if(h instanceof SketchEdge e) {
            if(e.start == last) {
                h = e.end;
            } else if(e.end == last) {
                h = e.start;
            }
        }
        if(h instanceof SketchVertex n) {
            if(this.makingFace) {
                if(this.faceVertices.isEmpty()) return false;
                if(!this.faceVertices.contains(n)) {
                    SketchEdge edge = last.getConnection(n);
                    if (edge == null) return false;

                    editorInstance.getSelectionManager().select(List.of(n, edge), EditorSelectionManager.SelectMode.ADD);
                    this.faceVertices.add(n);
                    this.face = newFace(this.faceVertices);
                    swapIfNeeded(this.face);
                } else if(this.faceVertices.get(0) == n && this.faceVertices.size() >= 3) {
                    this.face = newFace(this.faceVertices);
                    swapIfNeeded(this.face);
                    editorInstance.doAction(new AddElementsAction(List.of(face)));
                    stopFace(editorInstance);
                }
            } else {
                editorInstance.getSelectionManager().select(n, EditorSelectionManager.SelectMode.REPLACE);
                this.faceVertices.add(n);
                makingFace = true;
            }
        }
        return false;
    }

    SketchFace newFace(List<SketchVertex> vertices) {
        SketchFace face = new SketchFace(vertices);
        face.setDoubleSided(this.doubleSided);
        return face;
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
    }

    @Override
    public void render(EditorInstance editorInstance, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if(this.face != null && this.showFace) {
            VertexConsumerProvider.Immediate buffers = ModRenderLayers.getBuffers();
            VertexConsumer vc = buffers.getBuffer(ModRenderLayers.getPosColorTriangles());
            if(vc instanceof PosColorBufferBuilder pcbb) {
                WeaveRenderer.renderFace(pcbb, matrices, this.face);
            }
            buffers.draw();
            Vec3d mid = face.getAvgGlobalPos();
            Vec3d normal = face.getGlobalNormal();
            if(mid != null && normal != null) {
                WeaveRenderer.drawLine(vertexConsumers.getBuffer(RenderLayer.LINES), matrices, mid, mid.add(normal.multiply(0.25)), 0xBF, 0, 0x7F, 0xFF);
                if(this.doubleSided) {
                    WeaveRenderer.drawLine(vertexConsumers.getBuffer(RenderLayer.LINES), matrices, mid, mid.add(normal.multiply(-0.15)), 0x7F, 0, 0x5F, 0xFF);
                }
            }
        }
    }

    public void stopFace(EditorInstance editorInstance) {
        if(makingFace) {
            makingFace = false;
            faceVertices.clear();
            this.face = null;
            showFace = false;
            editorInstance.getSelectionManager().clearHighlight();
            editorInstance.getSelectionManager().clearSelection();
        }
    }

    void swapIfNeeded(SketchFace face) {
        Vec3d camLook = CameraController.getCameraLookVector(MinecraftClient.getInstance().gameRenderer.getCamera());
        Vec3d normal = face.getGlobalNormal();
        if(camLook.dotProduct(normal) > 0 == !this.flipSides) {
            face.swapFacing();
        }
    }

    public boolean getDoubleSided() {
        return this.doubleSided;
    }

    public void setDoubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
    }

    public boolean getFlipSides() {
        return this.flipSides;
    }

    public void setFlipSides(boolean flipSides) {
        this.flipSides = flipSides;
    }

    @Override
    public void setup(ToolSettingsWidget tsw) {
        super.setup(tsw);
        tsw.addBoolean(this, AddFaceTool::getDoubleSided, AddFaceTool::setDoubleSided, "doubleSided");
        tsw.addBoolean(this, AddFaceTool::getFlipSides, AddFaceTool::setFlipSides, "reverseSides");
    }
}
