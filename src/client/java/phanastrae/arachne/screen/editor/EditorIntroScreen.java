package phanastrae.arachne.screen.editor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import phanastrae.arachne.networking.screen_handler.SketchingTableScreenHandler;
import phanastrae.arachne.setup.ModItems;

import java.io.File;

public class EditorIntroScreen extends HandledScreen<SketchingTableScreenHandler> {
    public EditorIntroScreen(SketchingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    public File fileAutosave = new File(MinecraftClient.getInstance().runDirectory, "arachne/weaves/autosave.dat");

    ButtonWidget loadLast;

    TextWidget sketchText;
    boolean sketchPresent = false;

    @Override
    protected void init() {
        super.init();

        ButtonWidget createNew = new ButtonWidget.Builder(Text.of("Create New Weave"), b -> createNewWeave()).build();
        this.loadLast = new ButtonWidget.Builder(Text.of("Load Last Weave"), b -> loadLastWeave()).build();
        SimplePositioningWidget.setPos(createNew, 0, 0, this.width, this.height, 0.25f, 0.5f);
        SimplePositioningWidget.setPos(loadLast, 0, 0, this.width, this.height, 0.75f, 0.5f);
        this.addDrawableChild(createNew);
        this.addDrawableChild(loadLast);
        this.sketchText = new TextWidget(120, 30, Text.empty(), textRenderer);
        SimplePositioningWidget.setPos(this.sketchText, 0, 0, this.width, this.height, 0.5f, 0.25f);
        this.addDrawableChild(this.sketchText);

        checkAutosave();
    }

    @Override
    public void close() {
        super.close();
    }

    int tick = 0;
    boolean firstTick = true;

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        tick++;
        if(tick > 40) {
            tick = 0;
            checkAutosave();
        }

        ItemStack itemStack = this.getScreenHandler().getInventory().getStack(0);
        boolean sketchPresentNew = itemStack != null && (itemStack.isOf(ModItems.SKETCH) || itemStack.isOf(ModItems.FILLED_SKETCH));
        if(sketchPresentNew != sketchPresent || firstTick) {
            sketchPresent = sketchPresentNew;
            if(sketchPresentNew) {
                this.sketchText.setMessage(Text.of("Sketch Present in Table").copy().formatted(Formatting.GOLD));
            } else {
                this.sketchText.setMessage(Text.of("No Sketch in Table").copy().formatted(Formatting.GRAY));
            }
        }
        if(firstTick) { // TODO: check on multiplayer/with lag? just in case
            firstTick = false;

            if(itemStack.isOf(ModItems.FILLED_SKETCH)) {
                EditorMainScreen mls = this.makeMLS();
                mls.loadWeaveFromSketch(itemStack);
                MinecraftClient.getInstance().setScreen(mls);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(firstTick) return;
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        this.renderBackground(context);
    }

    public void checkAutosave() {
        if(this.loadLast != null) {
            this.loadLast.active = fileAutosave.exists() && fileAutosave.isFile();
        }
    }

    public EditorMainScreen makeMLS() {
        return new EditorMainScreen(this.getScreenHandler(), this.getScreenHandler().getPlayerInventory(), this.getTitle()); // TODO: check if good
    }

    public void createNewWeave() {
        EditorMainScreen mls = this.makeMLS();
        // TODO: is this the best way to do this?
        if(sketchPresent) {
            mls.loadWeaveFromSketch(ItemStack.EMPTY);
        }
        MinecraftClient.getInstance().setScreen(mls);
    }

    public void loadLastWeave() {
        EditorMainScreen mls = this.makeMLS();
        mls.loadLastWeave();
        // TODO: is this the best way to do this?
        if(sketchPresent) {
            mls.loadWeaveFromSketch(ItemStack.EMPTY);
        }
        MinecraftClient.getInstance().setScreen(mls);
    }
}
