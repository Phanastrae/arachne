package phanastrae.arachne.editor.tools;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.EditorSelectionManager;
import phanastrae.arachne.editor.RaycastResult;
import phanastrae.arachne.editor.editor_actions.AddElementsAction;
import phanastrae.arachne.editor.editor_actions.CompositeAction;
import phanastrae.arachne.editor.editor_actions.EditorAction;
import phanastrae.arachne.editor.editor_actions.RemoveElementsAction;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.screen.widget.BooleanPropertyWidget;
import phanastrae.arachne.screen.widget.ToolSettingsWidget;
import phanastrae.arachne.util.CenteredPlane;
import phanastrae.arachne.util.Line;
import phanastrae.arachne.util.SketchUtil;
import phanastrae.arachne.weave.WeaveRenderer;
import phanastrae.arachne.weave.element.sketch.*;

import java.util.ArrayList;
import java.util.List;

public class AddEdgeTool extends BasicTool {
    public static final Identifier TEXTURE = Arachne.id("textures/gui/editor/editor_tools.png");
    public static final int U = 16;
    public static final int V = 0;

    public boolean bisectLines = false;

    boolean doingEdge = false;

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
        return V + 16 * ((this.mod1held ? 1 : 0) + (this.mod2held ? 2 : 0));
    }

    @Override
    public String getId() {
        return "addEdge";
    }

    @Override
    public void onDeselect(EditorInstance editorInstance) {
        this.doingEdge = false;
        this.mod1held = false;
        this.mod2held = false;
        editorInstance.getSelectionManager().clearHighlight();
    }

    @Override
    public void tick(EditorInstance editorInstance, double mouseX, double mouseY) {
        editorInstance.getSelectionManager().clearHighlight();
        RaycastResult hit = editorInstance.rayCast(mouseX, mouseY);
        if(hit != null) {
            SketchElement e = hit.element();
            if(e instanceof SketchVertex || (e instanceof SketchEdge && bisectLines)) {
                if (!hit.element().selected || editorInstance.getSelectionManager().getSelection().size() > 1) {
                    editorInstance.getSelectionManager().highlight(hit.element());
                }
            }
        }
    }

    @Override
    public boolean clickMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        if(!doingEdge) {
            editorInstance.getSelectionManager().unselectIf((element) -> !(element instanceof SketchVertex));
            RaycastResult hit = editorInstance.rayCast(mouseX, mouseY);
            if (hit != null && hit.element() instanceof SketchVertex) {
                editorInstance.getSelectionManager().select(hit.element(), EditorSelectionManager.SelectMode.REPLACE);
            }
            boolean startEdge = editorInstance.getSelectionManager().hasSelection() || hit != null;
            if (startEdge) {
                this.doingEdge = true;
                return true;
            }
        } else {
            RaycastResult hit = editorInstance.rayCast(mouseX, mouseY);
            SketchElement latestNode = null;
            if(hit != null) {
                boolean b = false;
                if(hit.element() instanceof SketchVertex hitNode) {
                    latestNode = hitNode;
                    b = true;
                    ArrayList<SketchElement> newEdges = new ArrayList<>();
                    editorInstance.getSelectionManager().forEachSelected((element) -> {
                        if (element instanceof SketchVertex node) {
                            // cancel if connecting node to itself
                            if (node == hitNode) return;
                            List<SketchEdge> edges = node.getChildren();
                            if (edges != null) {
                                // cancel if edge already exists between these nodes
                                for (SketchElement e : edges) {
                                    if (e instanceof SketchEdge edge) {
                                        if (edge.start == hitNode || edge.end == hitNode) {
                                            return;
                                        }
                                    }
                                }
                            }
                            newEdges.add(new SketchEdge(node, hitNode));
                        }
                    });
                    if (newEdges.size() > 0) {
                        editorInstance.doAction(new AddElementsAction(newEdges));
                    }
                } else if(hit.element() instanceof SketchEdge sketchEdge && bisectLines) {
                    SketchTransform parent = SketchUtil.getCommonParent(List.of(sketchEdge.start, sketchEdge.end));
                    if(parent == null) {
                        parent = editorInstance.getSketchWeave().getRoot();
                    }
                    SketchVertexCollection vc = editorInstance.getOrCreateVertexCollection(parent);

                    b = true;
                    Line ray = editorInstance.getMouseRay(editorInstance.getScreen().mouseXtoScreenSpace(mouseX), editorInstance.getScreen().mouseYtoScreenSpace(mouseY));
                    Line edge = new Line(sketchEdge.start.getLastGlobalPos(), sketchEdge.end.getLastGlobalPos().subtract(sketchEdge.start.getLastGlobalPos()));
                    Vec3d nodePos = edge.findNearestPointToLine(ray);
                    if(nodePos != null) {
                        // convert nodePos back to local coords
                        Matrix4f locToGlo = vc.getTransform();
                        Matrix4f gloToLoc = locToGlo.invert();
                        Vector4f v = new Vector4f((float)nodePos.x, (float)nodePos.y, (float)nodePos.z, 1);
                        v = v.mul(gloToLoc);
                        nodePos = new Vec3d(v.x, v.y, v.z);

                        ArrayList<SketchElement> add = new ArrayList<>();
                        ArrayList<SketchElement> remove = new ArrayList<>();
                        SketchVertex newNode = new SketchVertex(vc);
                        editorInstance.getSelectionManager().forEachSelected((element) -> {
                            if (element instanceof SketchVertex node) {
                                if(node != sketchEdge.start && node != sketchEdge.end) {
                                    add.add(new SketchEdge(node, newNode));
                                }
                            }
                        });
                        if(add.size() >= 1) {
                            latestNode = newNode;
                            newNode.setPos(nodePos);
                            add.add(newNode);
                            SketchEdge edge1 = new SketchEdge(sketchEdge.start, newNode);
                            add.add(edge1);
                            SketchEdge edge2 = new SketchEdge(newNode, sketchEdge.end);
                            add.add(edge2);
                            remove.add(sketchEdge);
                            EditorAction addAction = new AddElementsAction(add);
                            EditorAction removeAction = RemoveElementsAction.of(remove);
                            if(removeAction == null) {
                                editorInstance.doAction(addAction);
                            } else {
                                editorInstance.doAction(new CompositeAction(List.of(addAction, removeAction)));
                            }
                        }
                    }
                }
                if(b && latestNode != null) {
                    if (mod1held && mod2held) {
                        editorInstance.getSelectionManager().select(latestNode, EditorSelectionManager.SelectMode.ADD);
                    } else if (mod1held && !mod2held) {
                        editorInstance.getSelectionManager().select(latestNode, EditorSelectionManager.SelectMode.REPLACE);
                    } else if (!mod1held && !mod2held) {
                        editorInstance.getSelectionManager().clearSelection();
                    }
                }
            }
            if(!mod1held && !mod2held) {
                this.doingEdge = false;
            }
            editorInstance.getSelectionManager().clearHighlight();
            return false;
        }
        return false;
    }

    @Override
    public boolean releaseMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        return false;
    }

    @Override
    public void render(EditorInstance editorInstance, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if(!doingEdge) return;

        Vec3d point = null;
        int r = 255;
        int g = 255;
        int b = 255;
        if(editorInstance.getSelectionManager().hasHighlight()) {
            SketchElement e = editorInstance.getSelectionManager().getHighlight().get(0);
            if(e instanceof SketchVertex n) {
                point = n.getLastGlobalPos();
            }
            r = 127;
            g = 255;
            b = 63;
        }
        if(point == null) {
            EditorMainScreen screen = editorInstance.getScreen();
            Line line = editorInstance.getMouseRay(screen.mouseXtoScreenSpace(screen.lastMouseX), screen.mouseYtoScreenSpace(screen.lastMouseY));
            CenteredPlane plane = new CenteredPlane(editorInstance.getScreen().globalToLocal(CameraController.getInstance().targetPos), line.offset);
            point = plane.intersectLine(line, 0);
        }
        if(point == null) return;

        VertexConsumer lines = vertexConsumers.getBuffer(RenderLayer.LINES);
        Vec3d finalPoint = point;
        int finalR = r;
        int finalG = g;
        int finalB = b;
        editorInstance.getSelectionManager().forEachSelected((element) -> {
            if(element instanceof SketchVertex node) {
                WeaveRenderer.drawLine(lines, matrices, node.getLastGlobalPos(), finalPoint, finalR, finalG, finalB, 255);
            }
        });
    }

    public boolean getBisectLines() {
        return this.bisectLines;
    }

    public void setBisectLines(boolean bisectLines) {
        this.bisectLines = bisectLines;
    }

    @Override
    public void setup(ToolSettingsWidget tsw) {
        super.setup(tsw);
        tsw.addBoolean(this, AddEdgeTool::getBisectLines, AddEdgeTool::setBisectLines, "bisectLines");
    }
}
