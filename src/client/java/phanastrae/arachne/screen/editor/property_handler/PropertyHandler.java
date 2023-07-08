package phanastrae.arachne.screen.editor.property_handler;

import net.minecraft.client.font.TextRenderer;

public class PropertyHandler {

    public PropertyHandler(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    protected TextRenderer textRenderer;
    protected PropertyListWidget propertyListWidget;

    public void link(PropertyListWidget plw) {
        this.propertyListWidget = plw;
        plw.clearChildren();
    }

    public void tick() {
    }
}
