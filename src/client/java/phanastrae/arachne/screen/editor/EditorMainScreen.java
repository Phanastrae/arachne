package phanastrae.arachne.screen.editor;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.EditorSelectionManager;
import phanastrae.arachne.editor.EditorTabHandler;
import phanastrae.arachne.editor.ToolBarWidget;
import phanastrae.arachne.editor.editor_actions.EditorAction;
import phanastrae.arachne.editor.editor_tabs.EditorTab;
import phanastrae.arachne.mixin.client.ScreenAccessor;
import phanastrae.arachne.networking.SketchUpdateC2SPacket;
import phanastrae.arachne.networking.screen_handler.SketchingTableScreenHandler;
import phanastrae.arachne.screen.widget.ListGridWidget;
import phanastrae.arachne.screen.widget.PropertyEditorWidget;
import phanastrae.arachne.screen.widget.SketchStructureViewWidget;
import phanastrae.arachne.screen.widget.ToolSettingsWidget;
import phanastrae.arachne.weave.NBTSerialization;
import phanastrae.arachne.weave.SketchWeave;
import phanastrae.arachne.weave.WeaveInstance;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class EditorMainScreen extends EditorScreen<SketchingTableScreenHandler> {

    public final EditorInstance editorInstance;

    PropertyEditorWidget propertyEditorWidget;
    SketchStructureViewWidget sketchStructureViewWidget;
    public ListGridWidget listGridWidget;
    ToolSettingsWidget toolSettingsWidget;
    boolean lgwFragile = false;

    public boolean writeToItem = false;

    @Nullable
    public WeaveInstance weaveInstance;
    boolean runWeave = false;

    public double lastMouseX = 0;
    public double lastMouseY = 0;

    public EditorMainScreen(SketchingTableScreenHandler handler, PlayerInventory inventory, Text title, SketchWeave sketchWeave) {
        super(handler, title);
        this.editorInstance = new EditorInstance(this, sketchWeave);
        this.setup();
    }

    public static SketchWeave loadFromFile(File file) {
        if(!file.exists() || !file.isFile()) return null;

        try {
            NbtCompound data = NbtIo.readCompressed(file);
            return loadFromNbt(data);
        } catch(Exception ignored) {
        }
        return null;
    }

    public static SketchWeave loadFromSketch(ItemStack stack) {
        if(stack == null || stack.isEmpty()) return null;
        NbtCompound nbt = stack.getNbt();
        if(nbt == null) return null;
        NbtCompound sketchData = nbt.getCompound("sketchData");
        if(sketchData == null) return null;
        return loadFromNbt(sketchData);
    }

    public static SketchWeave loadFromNbt(NbtCompound nbt) {
        if(nbt == null || nbt.isEmpty()) return null;

        Arachne.LOGGER.info("Reading Sketch");
        return NBTSerialization.readSketchWeave(nbt);
    }

    public void save() {
        NbtCompound nbt = NBTSerialization.writeSketchWeave(this.editorInstance.getSketchWeave());
        if(nbt == null) return;

        if(writeToItem) {
            saveToSketch();
        }

        Arachne.LOGGER.info("Saving Sketch to Autosave");
        savetoFile(getOrMakeAutosave(), nbt);
    }

    public void saveToSketch() {
        Arachne.LOGGER.info("Saving Sketch to Item");
        Vec3i pos = this.handler.getSketchPosition();
        ClientPlayNetworking.send(new SketchUpdateC2SPacket(this.editorInstance.getSketchWeave(), pos.getX(), pos.getY(), pos.getZ()));
    }

    public File getOrMakeAutosave() {
        File dirArachne = new File(MinecraftClient.getInstance().runDirectory, "arachne");
        dirArachne.mkdir();
        File dirWeaves = new File(dirArachne, "weaves");
        dirWeaves.mkdir();
        return new File(dirWeaves, "autosave.dat");
    }

    public void savetoFile(File file, NbtCompound nbt) {
        try {
            NbtIo.writeCompressed(nbt, file);
        } catch (IOException e) {
            Arachne.LOGGER.warn("Failed to save autosave sketch");
        }
    }

    @Override
    public void tick() {
        if(this.propertyEditorWidget != null) {
            this.propertyEditorWidget.tick();
        }
        if(this.toolSettingsWidget != null) {
            this.toolSettingsWidget.tick();
        }
    }

    public double mouseXtoScreenSpace(double mouseX) {
        return mouseX * 2f / this.width - 1;
    }

    public double mouseYtoScreenSpace(double mouseY) {
        return -(mouseY * 2f / this.height - 1);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        if(this.listGridWidget != null && this.listGridWidget.active) {
            this.listGridWidget.update();
        }
        if(this.toolSettingsWidget != null) {
            this.toolSettingsWidget.setTool(this.editorInstance.getTool());
        }

        this.editorInstance.tick(mouseX, mouseY);

        // super.render() without the unwanted parts
        this.drawBackground(context, delta, mouseX, mouseY);
        RenderSystem.disableDepthTest();
        for (Drawable drawable : ((ScreenAccessor)this).getDrawables()) {
            drawable.render(context, mouseX, mouseY, delta);
        }
        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 0.0f);
        this.drawForeground(context, mouseX, mouseY);
        context.getMatrices().pop();
        RenderSystem.enableDepthTest();
    }

    void setup() {
        CameraController.getInstance().onScreenOpen();
    }

    @Override
    protected void init() {
        EditorTabHandler tabHandler = this.editorInstance.getTabHandler();
        if(tabHandler.getActiveTab() == null) {
            tabHandler.setTab("main");
            if(tabHandler.getActiveTab() != null) {
                return; // setTab triggers init by itself, so do this to avoid double init
            }
        }

        initTabBar();

        initSidebar();

        this.setListGridWidget(this.makeListGridWidget());
        this.listGridWidget.visible = false;

        EditorTab tab = tabHandler.getActiveTab();
        if(tab != null) {
            tab.initScreen(this);
        }
    }

    public<T extends Element & Drawable & Selectable> void addChild(T w) {
        this.addDrawableChild(w);
    }

    @Override
    protected void clearChildren() {
        super.clearChildren();
        setPropertyEditorWidget(null);
        setListGridWidget(null);
        this.toolSettingsWidget = null;
        this.sketchStructureViewWidget = null;

        clearWeaveInstance();
    }

    public void setupWeaveInstance(boolean runWeave) {
        this.weaveInstance = new WeaveInstance(this.editorInstance.getSketchWeave().buildWeave());
        this.runWeave = runWeave;
    }

    public void clearWeaveInstance() {
        this.weaveInstance = null;
        this.runWeave = false;
    }

    public ListGridWidget makeListGridWidget() {
        return new ListGridWidget(this.textRenderer, 0, 0, 500, 300, Text.empty());
    }

    public void setListGridWidget(ListGridWidget listGridWidget) {
        this.listGridWidget = listGridWidget;
        if(listGridWidget != null) {
            positionWidget(listGridWidget, 0.5f, 0.5f);
            addDrawableChild(listGridWidget);
        }
    }

    public void setupListGridWidget(Consumer<ListGridWidget> update, boolean isFragile) {
        this.lgwFragile = isFragile;
        if(update == null) {
            this.listGridWidget.visible = false;
            this.listGridWidget.active = false;
            this.listGridWidget.setUpdate(null);
            this.listGridWidget.clearChildren();
        } else {
            this.listGridWidget.visible = true;
            this.listGridWidget.active = true;
            this.listGridWidget.setUpdate(update);
            this.listGridWidget.update();
        }
    }

    public PropertyEditorWidget makePropertyEditorWidget() {
        return new PropertyEditorWidget(this.editorInstance, this.textRenderer, 0, 0, 160, 240, Text.empty());
    }

    public void setPropertyEditorWidget(PropertyEditorWidget propertyEditorWidget) {
        this.propertyEditorWidget = propertyEditorWidget;
        if(propertyEditorWidget != null) {
            positionWidget(propertyEditorWidget, 0, 0.9f);
            addDrawableChild(propertyEditorWidget);
        }
    }

    public void positionWidget(Widget w, float relativeX, float relativeY) {
        SimplePositioningWidget.setPos(w, 0, 0, this.width, this.height, relativeX, relativeY);
    }

    void initTabBar() {
        List<ButtonWidget> tabs = new ArrayList<>();
        EditorTabHandler tabHandler = this.editorInstance.getTabHandler();
        EditorTab activeTab = tabHandler.getActiveTab();
        for(EditorTab tab : this.editorInstance.getTabs()) {
            Text name = tab.getName();
            ButtonWidget bw = ButtonWidget.builder(name, (b) -> tabHandler.setTab(tab)).width(120).build();
            if(tab == activeTab) {
                bw.active = false;
            }
            tabs.add(bw);
        }

        GridWidget gridWidget0 = new GridWidget();
        GridWidget.Adder adder0 = gridWidget0.createAdder(6);
        tabs.forEach(adder0::add);
        gridWidget0.refreshPositions();
        positionWidget(gridWidget0, 0.5f, 0);
        tabs.forEach(this::addDrawableChild);
    }

    void initSidebar() {
        EditorTabHandler tabHandler = this.editorInstance.getTabHandler();
        EditorTab tab = tabHandler.getActiveTab();
        if(tab == null) return;

        List<Pair<Text, Runnable>> actions = tab.getActions(this.editorInstance);
        if(actions.isEmpty()) return;

        List<ButtonWidget> buttons = new ArrayList<>();
        for(Pair<Text, Runnable> action : actions) {
            ButtonWidget button = ButtonWidget.builder(action.getLeft(), (b) -> action.getRight().run()).width(90).build();
            buttons.add(button);
        }

        GridWidget gridWidget = new GridWidget();
        GridWidget.Adder adder = gridWidget.createAdder(1);
        buttons.forEach(adder::add);
        gridWidget.refreshPositions();
        positionWidget(gridWidget, 1, 0.65f);
        buttons.forEach(this::addDrawableChild);
    }

    public void initToolBar() {
        ToolBarWidget toolBar = new ToolBarWidget(0, 0, Text.of("Toolbar"), this.editorInstance.getEditorToolHandler());
        this.positionWidget(toolBar, 0.5f, 1);
        this.addDrawableChild(toolBar);

        this.toolSettingsWidget = new ToolSettingsWidget(this.editorInstance, this.textRenderer, 0, 0, 180, 180, Text.of("Tool"));
        this.positionWidget(toolSettingsWidget, 0.85f, 1);
        this.addDrawableChild(toolSettingsWidget);

        //this.sketchStructureViewWidget = new SketchStructureViewWidget(this.editorInstance, this.textRenderer, 0, 0, 200, 200, Text.empty());
        //positionWidget(this.sketchStructureViewWidget, 1, 0);
        //this.addDrawableChild(this.sketchStructureViewWidget);
    }

    public void selectListGridElement(SketchElement element) {
        this.editorInstance.getSelectionManager().select(element, EditorSelectionManager.SelectMode.REPLACE);
    }

    public void setupForTab(@Nullable EditorTab tab) {
        this.clearAndInit();
        if(tab == null || !tab.getId().equals("main")) {
            this.editorInstance.getSelectionManager().clearSelection();
        }
    }

    @Override
    public void close() {
        save();

        CameraController.getInstance().RETURN_TO_PLAYER = true;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null) {
            Vec3i blockPos = this.getScreenHandler().getPosition();
            Vec3d lookTarget = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5);
            Vector2d yawPitch = CameraController.calcLookYawPitch(player.getEyePos(), lookTarget);
            player.setYaw((float)yawPitch.x);
            player.setPitch((float)yawPitch.y);
        }
        super.close();
    }

    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, "FPS: " + MinecraftClient.getInstance().getCurrentFps(), 0, 0, 0xFFFFFF, false);
        long l = Runtime.getRuntime().maxMemory();
        long m = Runtime.getRuntime().totalMemory();
        long n = Runtime.getRuntime().freeMemory();
        long o = m - n;
        context.drawText(this.textRenderer, String.format(Locale.ROOT, "Mem: % 2d%% %03d/%03dMB", o * 100L / l, o / 1024L / 1024L, l / 1024L / 1024L), 0, 10, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Nodes: " + editorInstance.getSketchWeave().getNodes().size(), 0, 20, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Edges: " + editorInstance.getSketchWeave().getEdges().size(), 0, 30, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Faces: " + editorInstance.getSketchWeave().getFaces().size(), 0, 40, 0xFFFFFF, false);

        ArrayList<EditorAction> actionQueue = this.editorInstance.getActionQueue().getActionQueue();
        int lastAction = this.editorInstance.getActionQueue().getNextAction() - 1;
        int start = lastAction - 8;
        int end = lastAction + 8;
        if(start < 0) {
            end -= start;
            start -= start;
        }
        if(end >= actionQueue.size()) {
            int d = actionQueue.size() - end - 1;
            end += d;
            start += d;
        }
        int j = 5;
        for(int i = start; i <= end; i++) {
            if(i < 0 || i >= actionQueue.size()) continue;
            EditorAction action = actionQueue.get(i);

            int color = i == lastAction ? 0x7FFF7F : i == lastAction + 1 ? 0xAF7F7FFF : i < lastAction ? 0xDFDFDF : 0x7F7F7FFF;
            context.drawText(this.textRenderer, action.getTitle(), 0, j * 10, color, i == lastAction || i == lastAction + 1);
            j++;
        }
    }

    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        this.editorInstance.getSelectionManager().draw(context, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(this.propertyEditorWidget != null && this.propertyEditorWidget.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(this.toolSettingsWidget != null && this.toolSettingsWidget.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(this.sketchStructureViewWidget != null && this.sketchStructureViewWidget.isFocused() && this.sketchStructureViewWidget.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if(this.editorInstance.getEditorToolHandler().handleKeyPress(keyCode, scanCode, modifiers)) {
            return true;
        }

        // TODO find a good way to select all that doesn't contradict existing controls but also makes sense
        if(keyCode == GLFW.GLFW_KEY_E && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            EditorTab tab = this.editorInstance.getTabHandler().getActiveTab();
            String id = tab == null ? null : tab.getId();
            if("main".equals(id)) {
                this.editorInstance.getSelectionManager().selectAll();
                return true;
            }
        }

        GameOptions options = MinecraftClient.getInstance().options;
        CameraController camControl = CameraController.getInstance();
        KeyBinding[] keys = {options.jumpKey, options.sneakKey, options.forwardKey, options.backKey, options.leftKey, options.rightKey};
        boolean[] pressed = new boolean[6];
        for(int i = 0; i < 6; i++) {
            if(keys[i].matchesKey(keyCode, scanCode)) {
                pressed[i] = true;
            }
        }
        if(pressed[0]) camControl.jumpHeld = true;
        if(pressed[1]) camControl.sneakHeld = true;
        if(pressed[2]) camControl.forwardHeld = true;
        if(pressed[3]) camControl.backwardHeld = true;
        if(pressed[4]) camControl.leftHeld = true;
        if(pressed[5]) camControl.rightHeld = true;
        if(pressed[0] || pressed[1] || pressed[2] || pressed[3] || pressed[4] || pressed[5]) {
            return true;
        }



        switch (keyCode) {
            case GLFW.GLFW_KEY_N -> {
                editorInstance.addVertex();
                return true;
            }
            case GLFW.GLFW_KEY_M -> {
                /*
                ModelPart model = MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.GUARDIAN);
                MatrixStack stack = new MatrixStack();
                stack.scale(-1.0f, -1.0f, 1.0f);
                stack.translate(0.0f, -1.501f, 0.0f);
                ArrayList<SketchTransform> rbs = new ArrayList<>();
                model.forEachCuboid(stack, (entry, string, i, cuboid) -> {
                    String s = string.substring(1);
                    String[] strings = s.split("/");
                    ModelPart part = model;
                    try {
                        for(String st : strings) {
                            part = part.getChild(st);
                        }
                    } catch(Exception e) {
                        Arachne.LOGGER.info("uh oh: " + string);
                        return;
                    }
                    Matrix4f mat = entry.getPositionMatrix();
                    SketchTransform transform = new SketchTransform(editorInstance.getSketchWeave().getRoot());
                    transform.sizex = (cuboid.maxX - cuboid.minX) / 16 * part.xScale;
                    transform.sizey = (cuboid.maxY - cuboid.minY) / 16 * part.yScale;
                    transform.sizez = (cuboid.maxZ - cuboid.minZ) / 16 * part.zScale;
                    Vector4f pos = new Vector4f((cuboid.maxX + cuboid.minX) / 32, (cuboid.maxY + cuboid.minY) / 32, (cuboid.maxZ + cuboid.minZ) / 32, 1);
                    pos.mul(mat);
                    transform.x = pos.x;
                    transform.y = pos.y;
                    transform.z = pos.z;
                    transform.setRollRad(part.roll);
                    transform.setYawRad(-part.yaw);
                    transform.setPitchRad(-part.pitch);
                    //transform.setPosition(globalToLocal(camControl.targetPos));
                    rbs.add(transform);
                });
                if(!rbs.isEmpty()) {
                    AddElementsAction add = new AddElementsAction(rbs);
                    editorInstance.doAction(add);
                }
                return true;
                */
            }
            case GLFW.GLFW_KEY_Z -> {
                if((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    editorInstance.undo();
                }
                return true;
            }
            case GLFW.GLFW_KEY_Y -> {
                if((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    editorInstance.redo();
                }
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                editorInstance.deleteSelected();
            }
            case GLFW.GLFW_KEY_T -> {
                /*
                List<SketchTransform> newRBs = new ArrayList<>();
                for(int i = 0; i < 360; i++) {
                    SketchTransform child = new SketchTransform(i == 0 ? editorInstance.getSketchWeave().getRoot() : newRBs.get(i-1));
                    child.y = 2;
                    newRBs.add(child);
                }
                AddElementsAction add = new AddElementsAction(newRBs);
                this.editorInstance.doAction(add);
                */
            }
        }
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(this.propertyEditorWidget != null && this.propertyEditorWidget.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(this.toolSettingsWidget != null && this.toolSettingsWidget.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(this.sketchStructureViewWidget != null && this.sketchStructureViewWidget.isFocused() && this.sketchStructureViewWidget.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }

        if(this.editorInstance.getEditorToolHandler().handleKeyRelease(keyCode, scanCode, modifiers)) {
            return true;
        }
        // TODO: try to deduplicate code?
        GameOptions options = MinecraftClient.getInstance().options;
        CameraController camControl = CameraController.getInstance();
        KeyBinding[] keys = {options.jumpKey, options.sneakKey, options.forwardKey, options.backKey, options.leftKey, options.rightKey};
        boolean[] released = new boolean[6];
        for(int i = 0; i < 6; i++) {
            if(keys[i].matchesKey(keyCode, scanCode)) {
                released[i] = true;
            }
        }
        if(released[0]) camControl.jumpHeld = false;
        if(released[1]) camControl.sneakHeld = false;
        if(released[2]) camControl.forwardHeld = false;
        if(released[3]) camControl.backwardHeld = false;
        if(released[4]) camControl.leftHeld = false;
        if(released[5]) camControl.rightHeld = false;
        if(released[0] || released[1] || released[2] || released[3] || released[4] || released[5]) {
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {

        if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            CameraController.getInstance().mouseDragged(deltaX, deltaY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(this.getFocused() != null) {
            if(this.getFocused().mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
        }

        CameraController.getInstance().mouseScrolled(amount);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.lgwFragile) {
            if(this.listGridWidget != null && this.listGridWidget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else {
                this.setupListGridWidget(null, false);
            }
        }

        for (Element element : this.children()) {
            if(element.mouseClicked(mouseX, mouseY, button)) {
                if(this.getFocused() != element) {
                    this.setFocused(element);
                }
                if (button == 0) {
                    this.setDragging(true);
                }
                return true;
            }
        }

        if(this.getFocused() != null) {
            this.setFocused(null);
            return true;
        }

        if(editorInstance.getEditorToolHandler().handleMouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(this.getFocused() != null) {
            this.getFocused().mouseReleased(mouseX, mouseY, button);
            return true;
        }

        if(editorInstance.getEditorToolHandler().handleMouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return false;
    }

    public static void tickFromBlockEntity(BlockPos blockPos) {
        // tick the weave when the block entity gets ticked
        if(MinecraftClient.getInstance().currentScreen instanceof EditorMainScreen screen) {
            if(screen.getScreenHandler().getPosition().equals(blockPos)) {
                screen.tickWeave();
            }
        }
    }

    public void tickWeave() {
        if(this.weaveInstance != null && runWeave) {
            this.weaveInstance.preUpdate(MinecraftClient.getInstance().world);
            this.weaveInstance.update();
        }
    }

    public Vec3d getOriginPos() {
        Vec3i blockPos = this.getScreenHandler().getPosition();
        return new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5);
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    public Vec3d globalToLocal(Vec3d pos) {
        return pos.subtract(getOriginPos());
    }

    public Vec3d localToGlobal(Vec3d pos) {
        return pos.add(getOriginPos());
    }

    public void onSelectionChanged() {
        if(this.propertyEditorWidget != null) {
            this.propertyEditorWidget.onSelectionChanged();
        }
    }
}
