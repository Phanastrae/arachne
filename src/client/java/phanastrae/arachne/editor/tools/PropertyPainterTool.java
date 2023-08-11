package phanastrae.arachne.editor.tools;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.RaycastResult;
import phanastrae.arachne.editor.editor_actions.*;
import phanastrae.arachne.screen.widget.ElementPropertyWidget;
import phanastrae.arachne.screen.widget.IntegerPropertyWidget;
import phanastrae.arachne.screen.widget.ToolSettingsWidget;
import phanastrae.arachne.weave.element.sketch.*;
import phanastrae.old.RenderMaterial;

import java.util.ArrayList;
import java.util.List;

public class PropertyPainterTool extends BasicTool {
    public static final Identifier TEXTURE = Arachne.id("textures/gui/editor/editor_tools.png");
    public static final int U = 96;
    public static final int V = 0;

    static int MAX_LAYER = 3;
    int layer = 0;
    SketchRenderMaterial renderMaterial = null;
    int r = 255;
    int g = 255;
    int b = 255;
    int a = 255;
    double u = 0;
    double v = 0;
    boolean doU = true;
    boolean doV = true;

    boolean mouseHeld = false;

    ModifiableAction action = null;
    List<SketchElement> add = null;
    List<SketchElement> remove = null;

    boolean showingPreview = false;

    boolean uvMode = false;

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
        return V + (mod1held ? 16 : 0);
    }

    @Override
    public String getId() {
        return "propertyPainter";
    }

    @Override
    public void onSelect(EditorInstance editorInstance) {
        editorInstance.getSelectionManager().clearSelection();
    }

    @Override
    public void onDeselect(EditorInstance editorInstance) {
        editorInstance.getSelectionManager().clearHighlight();
        mouseHeld = false;
        if(showingPreview) {
            closePreview(editorInstance);
        }
    }

    @Override
    public void tick(EditorInstance editorInstance, double mouseX, double mouseY) {
        if(mod2held && !showingPreview) {
            openPreview(editorInstance);
        } else if(!mod2held && showingPreview) {
            closePreview(editorInstance);
        }
        Pair<SketchFace, SketchVertex> hit = getHit(editorInstance, mouseX, mouseY);
        SketchFace face = hit.getLeft();
        SketchVertex vertex = hit.getRight();

        editorInstance.getSelectionManager().clearHighlight();
        if(face != null) {
            editorInstance.getSelectionManager().highlight(face);
        }
        if(vertex != null && uvMode) {
            editorInstance.getSelectionManager().highlight(vertex);
        }
        if(mouseHeld) {
            paint(editorInstance, hit);
        }
    }

    @Override
    public boolean clickMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        if(uvMode) {
            Pair<SketchFace, SketchVertex> fv = getHit(editorInstance, mouseX, mouseY);
            SketchFace face = fv.getLeft();
            SketchVertex vertex = fv.getRight();
            if(face != null && vertex != null) {
                int id = -1;
                for(int i = 0; i < face.vertices.length; i++) {
                    if(face.vertices[i] == vertex) {
                        id = i;
                        break;
                    }
                }
                if(id == -1) return false;

                if(mod1held) {
                    if(doU) {
                        this.u = face.u[this.layer][id];
                    }
                    if(doV) {
                        this.v = face.v[this.layer][id];
                    }
                } else {
                    int finalId = id;
                    ArrayList<EditorAction> list = new ArrayList<>();
                    if(doU) {
                        EditorAction changeU = new ModifyVariableAction<>(List.of(face), List.of(face.u[this.layer][id]), (float) this.u, (f, u) -> {
                            f.u[this.layer][finalId] = u;
                        });
                        list.add(changeU);
                    }
                    if(doV) {
                        EditorAction changeV = new ModifyVariableAction<>(List.of(face), List.of(face.v[this.layer][id]), (float) this.v, (f, v) -> {
                            f.v[this.layer][finalId] = v;
                        });
                        list.add(changeV);
                    }
                    if(!list.isEmpty()) {
                        EditorAction action = new CompositeAction(list);
                        editorInstance.doAction(action);
                    }
                }
                return true;
            }
        } else {
            if (!mouseHeld) {
                if (mod1held) {
                    SketchFace face = getHit(editorInstance, mouseX, mouseY).getLeft();
                    if (face != null) {
                        this.renderMaterial = face.getRenderMaterial(this.layer);
                        this.r = face.getR(this.layer);
                        this.g = face.getG(this.layer);
                        this.b = face.getB(this.layer);
                        this.a = face.getA(this.layer);
                    }
                } else {
                    mouseHeld = true;
                    paint(editorInstance, getHit(editorInstance, mouseX, mouseY));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean releaseMouse(EditorInstance editorInstance, double mouseX, double mouseY) {
        if(mouseHeld) {
            mouseHeld = false;
            finishPaint(editorInstance);
            return true;
        }
        return false;
    }

    public void paint(EditorInstance editorInstance, Pair<SketchFace, SketchVertex> pair) {
        SketchFace face = pair.getLeft();
        SketchVertex vertex = pair.getRight();
        if(face == null) return;

        if(action == null || !action.canEdit()) {
            startPaint(editorInstance);
        }

        if(action.canEdit() && !face.selected) {
            editorInstance.getActionQueue().updateLast(() -> {
                add.add(face);
            });
        }
    }

    void startPaint(EditorInstance editorInstance) {
        if(!editorInstance.getSelectionManager().getSelection().isEmpty()) {
            editorInstance.getSelectionManager().clearSelection();
        }

        this.add = new ArrayList<>();
        this.remove = new ArrayList<>();

        this.action = new ChangeSelectionAction(this.add, this.remove);
        editorInstance.doAction(this.action);
    }

    void finishPaint(EditorInstance editorInstance) {
        this.action = null;
        this.add = null;
        this.remove = null;

        List<SketchFace> faces = new ArrayList<>();
        List<SketchRenderMaterial> renderMaterials = new ArrayList<>();
        List<Integer> rs = new ArrayList<>();
        List<Integer> gs = new ArrayList<>();
        List<Integer> bs = new ArrayList<>();
        List<Integer> as = new ArrayList<>();
        for(SketchElement e : editorInstance.getSelectionManager().getSelection()) {
            if(e instanceof SketchFace f) {
                faces.add(f);
                renderMaterials.add(f.getRenderMaterial(this.layer));
                rs.add(f.getR(this.layer));
                gs.add(f.getG(this.layer));
                bs.add(f.getB(this.layer));
                as.add(f.getA(this.layer));
            }
        }

        if(faces.isEmpty()) return;

        ModifyVariableAction<SketchFace, SketchRenderMaterial> renderMaterial = new ModifyVariableAction<>(faces, renderMaterials, this.renderMaterial, (sf, rm) -> sf.setRenderMaterial(rm, this.layer));
        ModifyVariableAction<SketchFace, Integer> r = new ModifyVariableAction<>(faces, rs, this.r, (sf, v) -> sf.setR(v, this.layer));
        ModifyVariableAction<SketchFace, Integer> g = new ModifyVariableAction<>(faces, gs, this.g, (sf, v) -> sf.setG(v, this.layer));
        ModifyVariableAction<SketchFace, Integer> b = new ModifyVariableAction<>(faces, bs, this.b, (sf, v) -> sf.setB(v, this.layer));
        ModifyVariableAction<SketchFace, Integer> a = new ModifyVariableAction<>(faces, as, this.a, (sf, v) -> sf.setA(v, this.layer));
        CompositeAction action = new CompositeAction(List.of(renderMaterial, r, g, b, a));
        action.setTitle(Text.of("Painted Materials"));
        editorInstance.doAction(action);
        editorInstance.getSelectionManager().clearSelection();

        if(showingPreview) {
            closePreview(editorInstance);
            openPreview(editorInstance);
        }
    }

    public Pair<SketchFace, SketchVertex> getHit(EditorInstance editorInstance, double mouseX, double mouseY) {
        RaycastResult raycastResult = editorInstance.rayCast(mouseX, mouseY);
        if(raycastResult != null) {
            SketchElement element = raycastResult.element();
            if(element instanceof SketchFace face) {
                SketchVertex v = null;
                double d = Double.POSITIVE_INFINITY;
                for(SketchVertex vertex : face.vertices) {
                    double dist = vertex.getPos().subtract(raycastResult.pos()).lengthSquared();
                    if(dist < d) {
                        d = dist;
                        v = vertex;
                    }
                }
                return new Pair<>(face, v);
            }
        }
        return new Pair<>(null, null);
    }

    public void openPreview(EditorInstance editorInstance) {
        this.showingPreview = true;
        editorInstance.getScreen().setupWeaveInstance(false);
    }

    public void closePreview(EditorInstance editorInstance) {
        this.showingPreview = false;
        editorInstance.getScreen().clearWeaveInstance();
    }

    public int getLayer() {
        return this.layer;
    }

    public void setLayer(int layer) {
        if(layer < 0) layer = 0;
        if(layer > MAX_LAYER) layer = MAX_LAYER;
        this.layer = layer;
    }

    public SketchRenderMaterial getRenderMaterial() {
        return this.renderMaterial;
    }

    public void setRenderMaterial(SketchRenderMaterial sketchRenderMaterial) {
        this.renderMaterial = sketchRenderMaterial;
    }

    public int getR() {
        return this.r;
    }

    public int getG() {
        return this.g;
    }

    public int getB() {
        return this.b;
    }

    public int getA() {
        return this.a;
    }

    public void setR(int r) {
        this.r = r;
    }

    public void setG(int g) {
        this.g = g;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setA(int a) {
        this.a = a;
    }

    public boolean isUVmode() {
        return uvMode;
    }

    public void setRenderMaterialMode(ToolSettingsWidget tsw) {
        EditorInstance editorInstance = tsw.getEditorInstance();
        this.uvMode = false;
        tsw.setTool(this, true);
    }

    public void setUVmode(ToolSettingsWidget tsw) {
        EditorInstance editorInstance = tsw.getEditorInstance();
        this.uvMode = true;
        tsw.setTool(this, true);
        if(mouseHeld) {
            finishPaint(editorInstance);
        }
    }

    public double getUVal() {
        return this.u;
    }

    public double getVVal() {
        return this.v;
    }

    public void setUVal(double u) {
        this.u = Math.clamp(0, 1, u);
    }

    public void setVVal(double v) {
        this.v = Math.clamp(0, 1, v);
    }

    public boolean getDoU() {
        return this.doU;
    }

    public boolean getDoV() {
        return this.doV;
    }

    public void setDoU(boolean b) {
        this.doU = b;
    }

    public void setDoV(boolean b) {
        this.doV = b;
    }

    @Override
    public void setup(ToolSettingsWidget tsw) {
        super.setup(tsw);
        ButtonWidget b1 = ButtonWidget.builder(Text.of("Ren Mats"), (b) -> this.setRenderMaterialMode(tsw)).build();
        ButtonWidget b2 = ButtonWidget.builder(Text.of("UVs"), (b) -> this.setUVmode(tsw)).build();
        if(uvMode) {
            b2.active = false;
        } else {
            b1.active = false;
        }
        tsw.addChild(b1);
        tsw.addChild(b2);
        tsw.addLine();
        tsw.addSeparator();

        tsw.addInteger(this, PropertyPainterTool::getLayer, PropertyPainterTool::setLayer, "layer").setBounds(0, MAX_LAYER);
        if(uvMode) {
            tsw.addDouble(this, PropertyPainterTool::getUVal, PropertyPainterTool::setUVal, "u").setBounds(0, 1);
            tsw.addDouble(this, PropertyPainterTool::getVVal, PropertyPainterTool::setVVal, "v").setBounds(0, 1);
            tsw.addBoolean(this, PropertyPainterTool::getDoU, PropertyPainterTool::setDoU, "doU");
            tsw.addBoolean(this, PropertyPainterTool::getDoV, PropertyPainterTool::setDoV, "doV");
        } else {
            tsw.addRenderMaterial(this, PropertyPainterTool::getRenderMaterial, PropertyPainterTool::setRenderMaterial, "renderMaterial.short", () -> tsw.getEditorInstance().getSketchWeave().getRenderMaterials());
            tsw.addInteger(this, PropertyPainterTool::getR, PropertyPainterTool::setR, "r").setBounds(0, 255);
            tsw.addInteger(this, PropertyPainterTool::getG, PropertyPainterTool::setG, "g").setBounds(0, 255);
            tsw.addInteger(this, PropertyPainterTool::getB, PropertyPainterTool::setB, "b").setBounds(0, 255);
            tsw.addInteger(this, PropertyPainterTool::getA, PropertyPainterTool::setA, "a").setBounds(0, 255);
        }

    }
}
