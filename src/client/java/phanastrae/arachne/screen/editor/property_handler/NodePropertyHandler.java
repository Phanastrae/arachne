package phanastrae.arachne.screen.editor.property_handler;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import phanastrae.arachne.weave.Node;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class NodePropertyHandler extends PropertyHandler {

    public NodePropertyHandler(TextRenderer textRenderer) {
        super(textRenderer);
    }

    List<Node> nodeList;

    DoublePropertyWidget mpw;
    DoublePropertyWidget xpw;
    DoublePropertyWidget ypw;
    DoublePropertyWidget zpw;

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public void setMass(double d) {
        if(this.nodeList == null) return;
        for(Node node : this.nodeList) {
            node.setMass(d);
        }
    }

    public void setX(double d) {
        if(this.nodeList == null) return;
        for(Node node : this.nodeList) {
            node.setX(d);
        }
    }

    public void setY(double d) {
        if(this.nodeList == null) return;
        for(Node node : this.nodeList) {
            node.setY(d);
        }
    }

    public void setZ(double d) {
        if(this.nodeList == null) return;
        for(Node node : this.nodeList) {
            node.setZ(d);
        }
    }

    @Override
    public void link(PropertyListWidget plw) {
        super.link(plw);

        int w = plw.getInteriorWidth();
        this.xpw = new DoublePropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        this.mpw = new DoublePropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        this.ypw = new DoublePropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        this.zpw = new DoublePropertyWidget(textRenderer, 0, 0, w, 15, Text.empty());
        this.mpw.setConditionalChangedListeners(this::setMass, null);
        this.xpw.setConditionalChangedListeners(this::setX, null);
        this.ypw.setConditionalChangedListeners(this::setY, null);
        this.zpw.setConditionalChangedListeners(this::setZ, null);
        this.mpw.setTextForEmpty(Text.of("mass"));
        this.xpw.setTextForEmpty(Text.of("x"));
        this.ypw.setTextForEmpty(Text.of("y"));
        this.zpw.setTextForEmpty(Text.of("z"));
        plw.addChild(this.mpw);
        plw.addChild(this.xpw);
        plw.addChild(this.ypw);
        plw.addChild(this.zpw);

        this.tick();
    }

    @Override
    public void tick() {
        if(this.propertyListWidget == null) return;
        if(this.nodeList == null) return;

        handleGet(this.mpw, nodeList, Node::getMass);
        handleGet(this.xpw, nodeList, Node::getX);
        handleGet(this.ypw, nodeList, Node::getY);
        handleGet(this.zpw, nodeList, Node::getZ);

        for(Widget w : this.propertyListWidget.getChildren()) { // TODO: review
            if(w instanceof TextPropertyWidget tpw) {
                tpw.active = !nodeList.isEmpty();
            }
        }
    }

    public static void handleGet(TextPropertyWidget tpw, List<Node> nodeList, Function<Node, Double> fnc) { // TODO: tidy
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
