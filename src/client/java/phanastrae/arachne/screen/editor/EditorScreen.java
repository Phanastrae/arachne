package phanastrae.arachne.screen.editor;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class EditorScreen<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    protected final T handler;
    protected int backgroundWidth = 176;
    protected int backgroundHeight = 166;
    protected int x;
    protected int y;

    protected EditorScreen(T handler, Text title) {
        super(title);
        this.handler = handler;
    }

    @Override
    protected void init() {
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public T getScreenHandler() {
        return this.handler;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.client.player.isAlive() || this.client.player.isRemoved()) {
            this.client.player.closeHandledScreen();
        }
    }

    @Override
    public void removed() {
        if (this.client.player == null) {
            return;
        }
        this.handler.onClosed(this.client.player);
    }

    @Override
    public void close() {
        this.client.player.closeHandledScreen();
        super.close();
    }
}
