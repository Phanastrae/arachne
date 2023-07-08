package phanastrae.arachne.screen.editor.property_handler;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.text.Text;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.weave.RenderMaterial;
import phanastrae.arachne.weave.Weave;

public class RenderMaterialPropertyHandler extends PropertyHandler {

    public RenderMaterialPropertyHandler(TextRenderer textRenderer) {
        super(textRenderer);
    }

    Weave weave;

    int selectedId = 0;
    RenderMaterial selectedMaterial;

    public IntegerPropertyWidget idSelectionWidget;
    public TextPropertyWidget nameWidget;
    public TextPropertyWidget namespaceWidget;
    public TextPropertyWidget pathWidget;

    public void setWeave(Weave weave) {
        this.weave = weave;
    }

    public void select(int i) {
        if(i < 0 || this.weave.renderMaterials.size() <= i) {
            this.selectedId = 0;
            this.selectedMaterial = null;
            return;
        }
        this.selectedId = i;

        int j = 0;
        for(RenderMaterial rm : this.weave.renderMaterials.values()) {
            if(i == j) { // TODO: is there a better way to do this
                this.selectedMaterial = rm;
            }
            j++;
        }
    }

    @Override
    public void link(PropertyListWidget plw) {
        super.link(plw);

        int w = plw.getInteriorWidth();
        ButtonWidget addRM = ButtonWidget.builder(Text.of("New Material"), (b) -> {
            if(this.weave == null) return;
            String s = "New Material";
            while(this.weave.renderMaterials.containsKey(s)) {
                s = s + "+";
            }
            this.weave.addRenderMaterial(s, MissingSprite.getMissingSpriteId());
            this.idSelectionWidget.setBounds(0, this.weave.renderMaterials.size() - 1);
            this.select(this.weave.renderMaterials.size() - 1);
            }).width(w).build();
        plw.addChild(addRM);

        this.idSelectionWidget = new IntegerPropertyWidget(textRenderer, 0, 0, w, 15, Text.of("id"));
        this.idSelectionWidget.setConditionalChangedListeners(this::select, null);
        this.idSelectionWidget.setBounds(0, this.weave.renderMaterials.size() - 1);
        plw.addChild(this.idSelectionWidget);
        this.nameWidget = new TextPropertyWidget(textRenderer, 0, 0, w, 15, Text.of("name"));
        this.namespaceWidget = new TextPropertyWidget(textRenderer, 0, 0, w, 15, Text.of("namespace"));
        this.pathWidget = new TextPropertyWidget(textRenderer, 0, 0, w, 15, Text.of("path"));
        this.nameWidget.setChangedListener((str) -> {
            if(!this.weave.renderMaterials.containsKey(str) && this.selectedMaterial != null) {
                this.weave.renderMaterials.remove(this.selectedMaterial.name);
                this.selectedMaterial.name = str;
                this.weave.renderMaterials.put(str, this.selectedMaterial);
                int i = 0;
                for(RenderMaterial material : this.weave.renderMaterials.values()) { // TODO: try to make swapping behaviour not exist
                    if(material == this.selectedMaterial) {
                        this.selectedId = i;
                    }
                    i++;
                }
            }
        });
        this.namespaceWidget.setChangedListener((str) -> {
            if(this.selectedMaterial != null) {
                this.selectedMaterial.setNamespace(str);
            }
        });
        this.pathWidget.setChangedListener((str) -> {
            if (this.selectedMaterial != null) {
                this.selectedMaterial.setPath(str);
            }
        });
        plw.addChild(this.nameWidget);
        plw.addChild(this.namespaceWidget);
        plw.addChild(this.pathWidget);

        this.tick();
    }

    @Override
    public void tick() {
        if(this.propertyListWidget == null) return;
        if(this.weave == null) return;

        if(this.weave.renderMaterials.size() <= 0) {
            this.idSelectionWidget.visible = false;
        } else {
            this.idSelectionWidget.visible = true;
            this.idSelectionWidget.setState(TextPropertyWidget.SelectionType.SINGLE, String.valueOf(this.selectedId));
        }

        if(this.selectedMaterial == null) {
            this.nameWidget.visible = false;
            this.namespaceWidget.visible = false;
            this.pathWidget.visible = false;
        } else {
            this.nameWidget.visible = true;
            this.namespaceWidget.visible = true;
            this.pathWidget.visible = true;
            boolean nullTexture = this.selectedMaterial.texture == null;
            this.nameWidget.setState(TextPropertyWidget.SelectionType.SINGLE, this.selectedMaterial.name);
            this.namespaceWidget.setState(TextPropertyWidget.SelectionType.SINGLE, nullTexture ? "" : this.selectedMaterial.texture.getNamespace());
            this.pathWidget.setState(TextPropertyWidget.SelectionType.SINGLE, nullTexture ? "" : this.selectedMaterial.texture.getPath());
        }
    }
}
