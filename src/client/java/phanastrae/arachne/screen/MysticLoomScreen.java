package phanastrae.arachne.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.networking.screen_handler.MysticLoomScreenHandler;
import phanastrae.arachne.weave.WeaveCache;
import phanastrae.arachne.weave.WeavePreviewRenderer;

public class MysticLoomScreen extends HandledScreen<MysticLoomScreenHandler> {
    private static final Identifier TEXTURE = Arachne.id("textures/gui/container/mystic_loom.png");
    WeaveCache weaveCache = new WeaveCache();
    @Nullable
    NbtCompound nbtSketch = null;

    public MysticLoomScreen(MysticLoomScreenHandler screenHandler, PlayerInventory inventory, Text title) {
        super(screenHandler, inventory, title);
        screenHandler.setInventoryChangeListener(this::onInventoryChanged);
        this.titleY -= 2;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        this.renderBackground(context);
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        Slot materialSlot = this.handler.getMaterialSlot();
        Slot sketchSlot = this.handler.getSketchSlot();
        if (!materialSlot.hasStack()) {
            context.drawTexture(TEXTURE, this.x + materialSlot.x, this.y + materialSlot.y, this.backgroundWidth, 0, 16, 16);
        }
        if (!sketchSlot.hasStack()) {
            context.drawTexture(TEXTURE, this.x + sketchSlot.x, this.y + sketchSlot.y, this.backgroundWidth + 16, 0, 16, 16);
        }
        WeavePreviewRenderer.render(this.weaveCache.getOrMakeWeave(nbtSketch), context, this.x + 103, this.y + 9, 64, 64, -1);
    }

    private void onInventoryChanged() {
        // TODO idk update weave i guess
        ItemStack sketchStack = this.handler.getSketchSlot().getStack();
        NbtCompound nbt = null;
        if(sketchStack != null && !sketchStack.isEmpty()) {
            nbt = sketchStack.getNbt();
        }
        NbtCompound nbtSketch = null;
        if(nbt != null && nbt.contains("sketchData", NbtElement.COMPOUND_TYPE)) {
            nbtSketch = nbt.getCompound("sketchData");
        }
        this.nbtSketch = nbtSketch;
    }
}
