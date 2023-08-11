package phanastrae.arachne.editor;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import phanastrae.arachne.editor.tools.EditorTool;

import java.awt.image.renderable.RenderContext;

public class ToolBarWidget extends ClickableWidget {

    EditorToolHandler editorToolHandler;

    public ToolBarWidget(int x, int y, Text message, EditorToolHandler editorToolHandler) {
        super(x, y, 182, 22, message);
        this.editorToolHandler = editorToolHandler;
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(WIDGETS_TEXTURE, this.getX(), this.getY(), 0, 0, this.width, this.height);
        int i = this.editorToolHandler.getToolIndex();
        context.drawBorder(this.getX() + 1 + 20 * i, this.getY() + 1, 20, 20, 0xFFFFFF00);

        RenderSystem.enableBlend();
        int j = 0;
        for(EditorTool tool : this.editorToolHandler.editorTools) {
            Identifier tex = tool.getTexture();
            if(tex != null) {
                context.drawTexture(tool.getTexture(), this.getX() + 3 + j * 20, this.getY() + 3, tool.getU(), tool.getV(), 16, 16);
            }
            j++;
        }
        RenderSystem.disableBlend();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        double d = (mouseX - (this.getX() + 1)) / (this.width + 2) * 9;
        int i = (int)d;
        if(i < 0) i = 0;
        if(i > 8) i = 8;
        this.editorToolHandler.setTool(i);
    }
}
