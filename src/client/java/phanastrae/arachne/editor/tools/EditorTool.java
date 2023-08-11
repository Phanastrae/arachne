package phanastrae.arachne.editor.tools;

import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.screen.widget.PropertyHandler;
import phanastrae.arachne.screen.widget.ToolSettingsWidget;
import phanastrae.arachne.weave.element.GTV;
import phanastrae.arachne.weave.element.sketch.SketchElement;

public interface EditorTool extends GTV {
    @Nullable
    Identifier getTexture();
    int getU();
    int getV();
    String getId();

    default void onSelect(EditorInstance editorInstance){}
    default void onDeselect(EditorInstance editorInstance){}
    default void tick(EditorInstance editorInstance, double mouseX, double mouseY){}
    default boolean clickMouse(EditorInstance editorInstance, double mouseX, double mouseY){return false;}
    default boolean releaseMouse(EditorInstance editorInstance, double mouseX, double mouseY){return false;}
    default boolean pressKey(EditorInstance editorInstance, int keyCode, int scanCode, int modifiers){return false;}
    default boolean releaseKey(EditorInstance editorInstance, int keyCode, int scanCode, int modifiers){return false;}
    default void onSelectionChanged(EditorInstance editorInstance){}
    default void render(EditorInstance editorInstance, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers){}

    default void setup(ToolSettingsWidget tsw){
        defaultSetup(this, tsw);
    }

    static void defaultSetup(EditorTool tool, ToolSettingsWidget tsw) {
        String id = tool.getId();
        if(id != null) {
            tsw.addChild(new TextWidget(Text.translatable("arachne.editor.tool." + id), tsw.getTextRenderer()));
        }
        tsw.addLine();
        tsw.addSeparator();
    }
}
