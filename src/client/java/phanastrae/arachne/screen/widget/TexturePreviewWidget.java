package phanastrae.arachne.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TexturePreviewWidget<T> implements Drawable, Element, Widget, Selectable {
    Function<T, String> namespaceGetter;
    Function<T, String> pathGetter;
    Function<T, Boolean> useTexAtlasGetter;

    List<T> list;

    int x;
    int y;
    int width;
    int height;

    public TexturePreviewWidget(Function<T, String> namespaceGetter, Function<T, String> pathGetter, Function<T, Boolean> useTexAtlasGetter, int width, int height) {
        this.namespaceGetter = namespaceGetter;
        this.pathGetter = pathGetter;
        this.useTexAtlasGetter = useTexAtlasGetter;
        this.width = width;
        this.height = height;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public String getNamespace() {
        if(this.list == null || this.list.isEmpty() || this.namespaceGetter == null) return null;
        String s = this.namespaceGetter.apply(this.list.get(0));
        for(T t : this.list) {
            if(this.namespaceGetter.apply(t) != s) {
                return null;
            }
        }
        return s;
    }

    public String getPath() {
        if(this.list == null || this.list.isEmpty() || this.pathGetter == null) return null;
        String s = this.pathGetter.apply(this.list.get(0));
        for(T t : this.list) {
            if(this.pathGetter.apply(t) != s) {
                return null;
            }
        }
        return s;
    }

    public Boolean getUseTexAtlas() {
        if(this.list == null || this.list.isEmpty() || this.useTexAtlasGetter == null) return null;
        Boolean b = this.useTexAtlasGetter.apply(this.list.get(0));
        for(T t : this.list) {
            if(this.useTexAtlasGetter.apply(t) != b) {
                return null;
            }
        }
        return b;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        String namespace = getNamespace();
        String path = getPath();
        Boolean b = getUseTexAtlas();
        if(namespace == null || path == null || b == null) return;

        Identifier id;
        try {
            id = new Identifier(namespace, path);
        } catch(InvalidIdentifierException e) {
            return;
        }

        if(b) {
            Function<Identifier, Sprite> ATLAS = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            Sprite sprite = ATLAS.apply(id);
            if(sprite != null) {
                context.drawSprite(x, y, 0, width, height, sprite);
            }
        } else {
            context.drawTexture(id, x, y, 0, 0, 128, 128, 128, 128);
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        Element.super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return Element.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return Element.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return Element.super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return Element.super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return Element.super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return Element.super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return Element.super.charTyped(chr, modifiers);
    }

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return Element.super.getNavigationPath(navigation);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return Element.super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Nullable
    @Override
    public GuiNavigationPath getFocusedPath() {
        return Element.super.getFocusedPath();
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return Element.super.getNavigationFocus();
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        //
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }
}
