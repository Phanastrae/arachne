package phanastrae.arachne.editor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import phanastrae.arachne.editor.tools.*;

import java.util.ArrayList;

public class EditorToolHandler {

    EditorInstance editorInstance;
    int toolIndex;
    ArrayList<EditorTool> editorTools = new ArrayList<>();

    public EditorToolHandler(EditorInstance editorInstance) {
        this.editorInstance = editorInstance;
        registerAllTools();
        setTool(0);
    }

    public int getToolIndex() {
        return this.toolIndex;
    }

    public void setTool(int i) {
        if(this.toolIndex == i) return;

        EditorTool currentTool = this.getActiveTool();
        if(currentTool != null) {
            currentTool.onDeselect(this.editorInstance);
        }
        this.toolIndex = i;
        currentTool = this.getActiveTool();
        if(currentTool != null) {
            currentTool.onSelect(this.editorInstance);
        }
    }

    @Nullable
    public EditorTool getActiveTool() {
        if(this.toolIndex < 0 || this.toolIndex >= this.editorTools.size()) {
            return null;
        } else {
            return this.editorTools.get(this.toolIndex);
        }
    }

    public void registerAllTools() {
        registerTool(new SelectTool());
        registerTool(new AddEdgeTool());
        registerTool(new AddFaceTool());
        registerTool(new MoveTool());
        registerTool(new EraseTool());
        registerTool(new PropertyPainterTool());
    }

    public void registerTool(EditorTool tool) {
        this.editorTools.add(tool);
    }

    public void tick(double mouseX, double mouseY) {
        EditorTool tool = this.getActiveTool();
        if(tool != null) {
            tool.tick(this.editorInstance, mouseX, mouseY);
        }
    }

    public boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        KeyBinding[] hotbarKeys = MinecraftClient.getInstance().options.hotbarKeys;
        for(int i = 0; i < hotbarKeys.length; i++) {
            if(hotbarKeys[i].matchesKey(keyCode, scanCode)) {
                this.setTool(i);
                return true;
            }
        }
        EditorTool tool = this.getActiveTool();
        if(tool != null) {
            return tool.pressKey(this.editorInstance, keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean handleKeyRelease(int keyCode, int scanCode, int modifiers) {
        EditorTool tool = this.getActiveTool();
        if(tool != null) {
            return tool.releaseKey(this.editorInstance, keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        if(button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        EditorTool tool = this.getActiveTool();
        if(tool != null) {
            return tool.clickMouse(this.editorInstance, mouseX, mouseY);
        }
        return false;
    }

    public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        if(button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        EditorTool tool = this.getActiveTool();
        if(tool != null) {
            return tool.releaseMouse(this.editorInstance, mouseX, mouseY);
        }
        return false;
    }

    public void render(float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        EditorTool tool = this.getActiveTool();
        if(tool != null) {
            tool.render(this.editorInstance, tickDelta, matrices, vertexConsumers);
        }
    }

    public void onSelectionChanged() {
        EditorTool tool = this.getActiveTool();
        if(tool != null) {
            tool.onSelectionChanged(this.editorInstance);
        }
    }
}
