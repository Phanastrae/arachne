package phanastrae.arachne.screen.editor.property_handler;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import phanastrae.arachne.weave.link_type.Link;
import phanastrae.arachne.weave.link_type.StringLink;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

// TODO: change how links/edges/whatevers work or are called or whatever
public class EdgePropertyHandler extends PropertyHandler {

    public EdgePropertyHandler(TextRenderer textRenderer) {
        super(textRenderer);
    }

    List<Link> edgeList;

    DoublePropertyWidget lengthPW;
    DoublePropertyWidget constantPW;

    public void setEdgeList(List<Link> edgeList) {
        this.edgeList = edgeList;
    }

    public void setLength(double d) {
        if(this.edgeList == null) return;
        for(Link edge : this.edgeList) {
            if(edge instanceof StringLink) {
                ((StringLink) edge).idealLength = d;
            }
        }
    }

    public void setConstant(double d) {
        if(this.edgeList == null) return;
        for(Link edge : this.edgeList) {
            if(edge instanceof StringLink) {
                ((StringLink) edge).strengthConstant = d;
            }
        }
    }

    @Override
    public void link(PropertyListWidget plw) {
        super.link(plw);

        int w = plw.getInteriorWidth();
        this.lengthPW = new DoublePropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        this.constantPW = new DoublePropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        this.lengthPW.setConditionalChangedListeners(this::setLength, null);
        this.constantPW.setConditionalChangedListeners(this::setConstant, null);
        this.lengthPW.setTextForEmpty(Text.of("Ideal Length"));
        this.constantPW.setTextForEmpty(Text.of("Stiffness"));
        // TODO: bounds? non-negative?
        plw.addChild(this.lengthPW);
        plw.addChild(this.constantPW);

        // TODO: add proper boolean option
        ButtonWidget button = ButtonWidget.builder(Text.of("Toggle Spring/String mode"), (b) -> {
            if(this.edgeList == null) return;
            for(Link link : this.edgeList) {
                if(link instanceof StringLink stringLink) {
                    stringLink.pullOnly = !stringLink.pullOnly;
                }
            }}).width(w).build();
        plw.addChild(button);

        ButtonWidget button2 = ButtonWidget.builder(Text.of("Store Length"), (b) -> {
            if(this.edgeList == null) return;
            for(Link link : this.edgeList) {
                if(link instanceof StringLink stringLink) {
                    stringLink.idealLength = stringLink.node1.pos.subtract(stringLink.node2.pos).length();
                }
            }}).width(w).build();
        plw.addChild(button2);

        this.tick();
    }

    @Override
    public void tick() {
        if(this.propertyListWidget == null) return;
        if(this.edgeList == null) return;

        handleGet(this.lengthPW, edgeList, (e) -> e instanceof StringLink ? ((StringLink) e).idealLength : 0.0); // TODO: handle the whole link / stringlink thing
        handleGet(this.constantPW, edgeList, (e) -> e instanceof StringLink ? ((StringLink) e).strengthConstant : 0.0);

        for(Widget w : this.propertyListWidget.getChildren()) { // TODO: review
            if(w instanceof TextPropertyWidget tpw) {
                tpw.active = !edgeList.isEmpty();
            }
        }
    }

    public static void handleGet(TextPropertyWidget tpw, List<Link> nodeList, Function<Link, Double> fnc) { // TODO: tidy // TODO: move to widgets and generalise
        String s = "";
        TextPropertyWidget.SelectionType st = TextPropertyWidget.SelectionType.EMPTY;
        if(!nodeList.isEmpty()) {
            st = TextPropertyWidget.SelectionType.SINGLE;
            Double d = fnc.apply(nodeList.get(0));
            // check if all are equal, if not set type as multi
            for(int i = 1; i < nodeList.size(); i++) { // TODO: can this be improved?
                if(!Objects.equals(fnc.apply(nodeList.get(i)), d)) {
                    st = TextPropertyWidget.SelectionType.MULTI;
                    break;
                }
            }
            // if all equal set string to the shared value
            if(st == TextPropertyWidget.SelectionType.SINGLE && d != null) {
                s = Double.toString(d);
            }
        }
        tpw.setState(st, s);
        tpw.tick();
    }
}
