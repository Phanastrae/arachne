package phanastrae.arachne.editor.tools;

import org.lwjgl.glfw.GLFW;
import phanastrae.arachne.editor.EditorInstance;

public abstract class BasicTool implements EditorTool {
    boolean gtv = false;

    boolean mod1held = false;
    boolean mod2held = false;

    @Override
    public boolean getGTV() {
        return this.gtv;
    }

    @Override
    public void setGTV(boolean b) {
        this.gtv = b;
    }

    @Override
    public boolean pressKey(EditorInstance editorInstance, int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            this.mod1held = true;
            return true;
        }
        if(keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            this.mod2held = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean releaseKey(EditorInstance editorInstance, int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            this.mod1held = false;
            return true;
        }
        if(keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            this.mod2held = false;
            return true;
        }
        return false;
    }
}
