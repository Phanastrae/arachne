package phanastrae.arachne.old.property_handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import phanastrae.arachne.old.EditorMainScreen;
import phanastrae.old.Face;
import phanastrae.old.RenderMaterial;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class FacePropertyHandler extends PropertyHandler {

    public FacePropertyHandler(TextRenderer textRenderer) {
        super(textRenderer);
    }

    List<Face> faceList;

    IntegerPropertyWidget rpw;
    IntegerPropertyWidget gpw;
    IntegerPropertyWidget bpw;
    TextPropertyWidget rmpw;

    public void setFaceList(List<Face> faceList) {
        this.faceList = faceList;
    }

    public void setR(int r) {
        if(this.faceList == null) return;
        for(Face face : this.faceList) {
            face.r = r;
        }
    }

    public void setG(int g) {
        if(this.faceList == null) return;
        for(Face face : this.faceList) {
            face.g = g;
        }
    }

    public void setB(int b) {
        if(this.faceList == null) return;
        for(Face face : this.faceList) {
            face.b = b;
        }
    }

    public void setRenderMaterial(String string) {
        if(string == null) return;
        if(!(MinecraftClient.getInstance().currentScreen instanceof EditorMainScreen screen)) return;
        if(string.equals("")) return;
        if(string.equals("none")) string = "";
        RenderMaterial renderMaterial = screen.phySys.renderMaterials.get(string);
        for(Face face : this.faceList) {
            face.renderMaterial = renderMaterial;
        }
    }

    @Override
    public void link(PropertyListWidget plw) {
        super.link(plw);

        int w = plw.getInteriorWidth();
        this.rpw = new IntegerPropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        this.gpw = new IntegerPropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        this.bpw = new IntegerPropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        // TODO: add visible invalid text thing?
        this.rmpw = new TextPropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        this.rpw.setConditionalChangedListeners(this::setR, null);
        this.gpw.setConditionalChangedListeners(this::setG, null);
        this.bpw.setConditionalChangedListeners(this::setB, null);
        this.rmpw.setChangedListener(this::setRenderMaterial);
        this.rpw.setTextForEmpty(Text.of("r"));
        this.gpw.setTextForEmpty(Text.of("g"));
        this.bpw.setTextForEmpty(Text.of("b"));
        this.rmpw.setTextForEmpty(Text.of("Render Material"));
        this.rpw.setBounds(0, 255);
        this.gpw.setBounds(0, 255);
        this.bpw.setBounds(0, 255);
        plw.addChild(this.rpw);
        plw.addChild(this.gpw);
        plw.addChild(this.bpw);
        plw.addChild(this.rmpw);
        // TODO: make not bad
        ButtonWidget autoUV = ButtonWidget.builder(Text.of("auto uv (quads)"), (b) -> {
            Random random = new Random();
            for(Face face : this.faceList) {
                if(face.nodes.length == 4) {
                    Vec3d d1 = face.nodes[1].pos.subtract(face.nodes[0].pos);
                    Vec3d d2 = face.nodes[2].pos.subtract(face.nodes[1].pos);
                    Vec3d d3 = face.nodes[3].pos.subtract(face.nodes[2].pos);
                    Vec3d d4 = face.nodes[0].pos.subtract(face.nodes[3].pos);
                    double l1 = Math.sqrt((d1.lengthSquared() + d3.lengthSquared()) / 2);
                    double l2 = Math.sqrt((d2.lengthSquared() + d4.lengthSquared()) / 2);
                    int l1pix = (int)Math.min(16, Math.max(1, Math.round(l1 * 16)));
                    int l2pix = (int)Math.min(16, Math.max(1, Math.round(l2 * 16)));
                    int o1pix = random.nextInt(0, 16 - l1pix + 1);
                    int o2pix = random.nextInt(0, 16 - l2pix + 1);
                    l1 = l1pix / 16.;
                    l2 = l2pix / 16.;
                    double o1 = o1pix / 16.;
                    double o2 = o2pix / 16.;
                    face.ul[0] = (float)o2;
                    face.vl[0] = (float)o1;
                    face.ul[1] = (float)o2;
                    face.vl[1] = (float)(o1 + l1);
                    face.ul[2] = (float)(o2 + l2);
                    face.vl[2] = (float)(o1 + l1);
                    face.ul[3] = (float)(o2 + l2);
                    face.vl[3] = (float)o1;
                }
            }
        }).width(w).build();
        plw.addChild(autoUV);

        this.tick();
    }

    @Override
    public void tick() {
        if(this.propertyListWidget == null) return;
        if(this.faceList == null) return;

        handleGet(this.rpw, faceList, (f) -> f.r);
        handleGet(this.gpw, faceList, (f) -> f.g);
        handleGet(this.bpw, faceList, (f) -> f.b);
        handleGet2(this.rmpw, faceList, (f) -> f.renderMaterial);

        for(Widget w : this.propertyListWidget.getChildren()) { // TODO: review
            if(w instanceof TextPropertyWidget tpw) {
                tpw.active = !faceList.isEmpty();
            }
        }
    }

    public static void handleGet(TextPropertyWidget tpw, List<Face> faceList, Function<Face, Integer> fnc) { // TODO: tidy
        String s = "";
        TextPropertyWidget.SelectionType st = TextPropertyWidget.SelectionType.EMPTY;
        if(!faceList.isEmpty()) {
            st = TextPropertyWidget.SelectionType.SINGLE;
            Integer d = fnc.apply(faceList.get(0));
            // check if all are equal, if not set type as multi
            for(int i = 1; i < faceList.size(); i++) { // TODO: can this be improved?
                if(!Objects.equals(fnc.apply(faceList.get(i)), d)) {
                    st = TextPropertyWidget.SelectionType.MULTI;
                    break;
                }
            }
            // if all equal set string to the shared value
            if(st == TextPropertyWidget.SelectionType.SINGLE && d != null) {
                s = Integer.toString(d);
            }
        }
        tpw.setState(st, s);
        tpw.tick();
    }

    public static void handleGet2(TextPropertyWidget tpw, List<Face> faceList, Function<Face, RenderMaterial> fnc) { // TODO: tidy
        String s = "";
        TextPropertyWidget.SelectionType st = TextPropertyWidget.SelectionType.EMPTY;
        if(!faceList.isEmpty()) {
            st = TextPropertyWidget.SelectionType.SINGLE;
            RenderMaterial d = fnc.apply(faceList.get(0));
            // check if all are equal, if not set type as multi
            for(int i = 1; i < faceList.size(); i++) { // TODO: can this be improved?
                if(!Objects.equals(fnc.apply(faceList.get(i)), d)) {
                    st = TextPropertyWidget.SelectionType.MULTI;
                    break;
                }
            }
            // if all equal set string to the shared value
            if(st == TextPropertyWidget.SelectionType.SINGLE && d != null) {
                s = d.name;
            }
        }
        tpw.setState(st, s);
        tpw.tick();
    }
}
