package phanastrae.arachne.old;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.networking.screen_handler.SketchingTableScreenHandler;
import phanastrae.arachne.setup.ModItems;
import phanastrae.arachne.mixin.client.ScreenAccessor;
import phanastrae.arachne.networking.SketchUpdateC2SPacket;
import phanastrae.old.Face;
import phanastrae.old.Node;
import phanastrae.old.link_type.Link;
import phanastrae.old.link_type.StringLink;
import phanastrae.arachne.old.property_handler.*;
import phanastrae.arachne.old.tools.*;
import phanastrae.arachne.util.Line;
import phanastrae.arachne.util.TimerHolder;
import phanastrae.arachne.util.TimerTreeNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class EditorMainScreen extends HandledScreen<SketchingTableScreenHandler> {

    public EditorMainScreen(SketchingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        CameraController.getInstance().onScreenOpen();

        ToolContainer selectToolPair = new ToolContainer(new SelectTool(), this);
        selectToolPair.setKeyCode(GLFW.GLFW_KEY_Y);
        ToolContainer dragToolPair = new ToolContainer(new DragTool(), this);
        dragToolPair.setKeyCode(GLFW.GLFW_KEY_M);
        ToolContainer staticToggleToolPair = new ToolContainer(new StaticToggleTool(), this);
        staticToggleToolPair.setKeyCode(GLFW.GLFW_KEY_B);
        ToolContainer stringLinkToolPair = new ToolContainer(new StringLinkTool(), this);
        stringLinkToolPair.setKeyCode(GLFW.GLFW_KEY_L);
        ToolContainer faceCreationToolPair = new ToolContainer(new FaceCreationTool(), this);
        faceCreationToolPair.setKeyCode(GLFW.GLFW_KEY_F);
        this.toolContainers = new ToolContainer[]{selectToolPair, dragToolPair, staticToggleToolPair, stringLinkToolPair, faceCreationToolPair};

        SimpleActionContainer addNode = new SimpleActionContainer(makeSimpleButtonDefault(this::addNode, "addNode"));
        addNode.setKeyCode(GLFW.GLFW_KEY_N);
        SimpleActionContainer eraseNode = new SimpleActionContainer(makeSimpleButtonDefault(this::eraseSelectedNode, "eraseNode"));
        eraseNode.setKeyCode(GLFW.GLFW_KEY_BACKSPACE); // TODO: make delete key also work
        SimpleActionContainer spawnGrid = new SimpleActionContainer(makeSimpleButtonDefault(this::spawnGrid, "spawnGrid"));
        spawnGrid.setKeyCode(GLFW.GLFW_KEY_G);
        SimpleActionContainer spawnCube = new SimpleActionContainer(makeSimpleButtonDefault(this::spawnCube, "spawnCube"));
        //SimpleActionContainer badSplit = new SimpleActionContainer(makeSimpleButtonDefault(this::splitLinkButBadly, "badSplit"));
        //badSplit.setKeyCode(GLFW.GLFW_KEY_T);
        //SimpleActionContainer badFaceSplit = new SimpleActionContainer(makeSimpleButtonDefault(this::subdivideAllFaces, "badFaceSplit"));
        //badFaceSplit.setKeyCode(GLFW.GLFW_KEY_Z);
        //SimpleActionContainer toggleAltCam = new SimpleActionContainer(makeSimpleButtonDefault(this::toggleAltCameraMode, "toggleAltCam"));
        SimpleActionContainer toggleTime = new SimpleActionContainer(makeSimpleButtonDefault(this::toggleTime, "toggleTime"));
        toggleTime.setKeyCode(GLFW.GLFW_KEY_P);
        SimpleActionContainer toggleWind = new SimpleActionContainer(makeSimpleButtonDefault(this::toggleWind, "toggleWind"));
        SimpleActionContainer toggleNodeVis = new SimpleActionContainer(makeSimpleButtonDefault(this::toggleNodeVisibility, "toggleNodeVisibility"));
        toggleNodeVis.setKeyCode(GLFW.GLFW_KEY_H);
        SimpleActionContainer toggleLinkVis = new SimpleActionContainer(makeSimpleButtonDefault(this::toggleLinkVisibility, "toggleLinkVisibility"));
        SimpleActionContainer toggleFaceVis = new SimpleActionContainer(makeSimpleButtonDefault(this::toggleFaceVisibility, "toggleFaceVisibility"));
        this.simpleActionContainers = new SimpleActionContainer[]{addNode, eraseNode, spawnCube, spawnGrid, toggleTime, toggleWind, toggleNodeVis, toggleLinkVis, toggleFaceVis};

        switchToTool(selectToolPair);

        this.phySys.doFloor = true;
    }

    public void saveWeave() {
        // TODO: tidy filesave
        NbtCompound nbtCompound = this.getNbt();

        if(writeToItem) {
            //ClientPlayNetworking.send(new SketchUpdateC2SPacket(this.phySys));
        }

        File dirArachne = new File(MinecraftClient.getInstance().runDirectory, "arachne");
        dirArachne.mkdir();
        File dirWeaves = new File(dirArachne, "weaves");
        dirWeaves.mkdir();
        File file3 = new File(dirWeaves, "autosave.dat");
        try {
            NbtIo.writeCompressed(nbtCompound, file3);
        } catch (IOException e) {
            Arachne.LOGGER.warn("Failed to save autosave weave");
        }
    }


    public NbtCompound getNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        this.phySys.writeToNBT(nbtCompound);
        return nbtCompound;
    }

    boolean writeToItem = false;

    public void loadWeaveFromSketch(ItemStack itemStack) {
        writeToItem = true;
        if(itemStack == null || itemStack.isEmpty()) return;
        if(!itemStack.isOf(ModItems.FILLED_SKETCH) || itemStack.getNbt() == null) return;
        NbtCompound nbt = itemStack.getNbt().getCompound("sketchData");
        this.readFromNbt(nbt);
    }

    public void loadLastWeave() {
        // TODO: tidy fileload
        NbtCompound nbtCompound = null;
        try {
            File file = new File(MinecraftClient.getInstance().runDirectory, "arachne/weaves/autosave.dat");
            if (file.exists() && file.isFile()) {
                nbtCompound = NbtIo.readCompressed(file);
            }
        } catch (Exception exception) {
            Arachne.LOGGER.warn("Failed to load autosave data");
        }
        this.readFromNbt(nbtCompound);
    }

    public void readFromNbt(NbtCompound nbt) {
        if(nbt != null) {
            this.phySys.readFromNBT(nbt);
        }
    }

    @Override
    public void close() {
        saveWeave();

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

    public Vec3d originPos = Vec3d.ZERO;

    public PhysicsSystem phySys = new PhysicsSystem();

    public boolean timePaused = true;
    public boolean renderNodes = true;
    public boolean renderLinks = true;
    public boolean renderFaces = true;
    public boolean showDebug = false;

    public Node highlightedNode = null;
    // TODO: tweak all the changes made by switch selection to a listy thing
    public Selection selection = new Selection();

    public Line mouseRay = new Line(new Vec3d(0, 0, 0), new Vec3d(0, 0, 1));
    public Line mouseRayWorld = new Line(new Vec3d(0, 0, 0), new Vec3d(0, 0, 1));
    // mouse x and mouse y in range (-1, 1)
    public double lastScreenSpaceMouseX = 0;
    public double lastScreenSpaceMouseY = 0;

    public ToolContainer toolContainer;
    ToolContainer[] toolContainers;
    SimpleActionContainer[] simpleActionContainers;

    PropertyListWidget propertyListWidget;
    NodePropertyHandler nodePropertyHandler;
    EdgePropertyHandler edgePropertyHandler;
    FacePropertyHandler facePropertyHandler;
    RenderMaterialPropertyHandler renderMaterialPropertyHandler;
    PropertyHandler activePropertyHandler;

    public SelectionMode selectionMode = SelectionMode.VERTEX;
    public enum SelectionMode {
        VERTEX,
        EDGE,
        FACE
    }

    @Override
    protected void init() { // TODO: fun fact, this happens every time window is resized, please adjust accordingly if needed.
        super.init();

        GridWidget topBar = new GridWidget();
        GridWidget.Adder adderTop = topBar.createAdder(3);
        adderTop.add(ButtonWidget.builder(Text.translatable("Vertices"), (b) -> {this.selectionMode = SelectionMode.VERTEX; setPropertyHandler(nodePropertyHandler);}).width(60).build()); // TODO: improve
        adderTop.add(ButtonWidget.builder(Text.translatable("Edges"), (b) -> {this.selectionMode = SelectionMode.EDGE; setPropertyHandler(edgePropertyHandler);}).width(60).build());
        adderTop.add(ButtonWidget.builder(Text.translatable("Faces"), (b) -> {this.selectionMode = SelectionMode.FACE; setPropertyHandler(facePropertyHandler);}).width(60).build());
        adderTop.add(ButtonWidget.builder(Text.translatable("RM"), (b) -> {this.selectionMode = SelectionMode.FACE; setPropertyHandler(renderMaterialPropertyHandler);}).width(60).build()); // TODO: disable selections or just move elsewhere
        topBar.refreshPositions();
        SimplePositioningWidget.setPos(topBar, 0, 0, this.width, this.height, 0.5f, 0);
        topBar.forEachChild(this::addDrawableChild);

        GridWidget rightBar = new GridWidget();
        GridWidget.Adder adderRight = rightBar.createAdder(1);
        for(SimpleActionContainer simpleActionContainer : simpleActionContainers) {
            adderRight.add(simpleActionContainer.button);
        }
        rightBar.refreshPositions();
        SimplePositioningWidget.setPos(rightBar, 0, 0, this.width, this.height, 1, 0.5f);
        rightBar.forEachChild(this::addDrawableChild);

        GridWidget leftBar = new GridWidget();
        GridWidget.Adder adderLeft = leftBar.createAdder(1);
        for(ToolContainer toolContainer : toolContainers) {
            adderLeft.add(toolContainer.button);
        }
        leftBar.refreshPositions();
        SimplePositioningWidget.setPos(leftBar, 0, 0, this.width, this.height, 0, 0.5f);
        leftBar.forEachChild(this::addDrawableChild);

        this.propertyListWidget = new PropertyListWidget(textRenderer, 100, 100, 150, 150, Text.empty());
        SimplePositioningWidget.setPos(this.propertyListWidget, 0, 0, this.width, this.height, 0.02f, 0.98f);
        this.nodePropertyHandler = new NodePropertyHandler(this.textRenderer);
        this.edgePropertyHandler = new EdgePropertyHandler(this.textRenderer);
        this.facePropertyHandler = new FacePropertyHandler(this.textRenderer);
        this.renderMaterialPropertyHandler = new RenderMaterialPropertyHandler(this.textRenderer);
        nodePropertyHandler.setNodeList(this.selection.getNodes());
        edgePropertyHandler.setEdgeList(this.selection.getEdges());
        facePropertyHandler.setFaceList(this.selection.getFaces());
        renderMaterialPropertyHandler.setWeave(this.phySys);
        setPropertyHandler(nodePropertyHandler);
    }

    public void setPropertyHandler(PropertyHandler propertyHandler) {
        if(this.activePropertyHandler != propertyHandler) {
            this.activePropertyHandler = propertyHandler;
            if(propertyHandler != null) {
                propertyHandler.link(propertyListWidget);
                this.propertyListWidget.active = true;
                this.propertyListWidget.visible = true;
            } else {
                this.propertyListWidget.clearChildren();
                this.propertyListWidget.active = false;
                this.propertyListWidget.visible = false;
            }
        }
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if(this.activePropertyHandler != null) {
            this.activePropertyHandler.tick();
        }
    }

    static class SimpleActionContainer {
        ButtonWidget button;
        private int keyCode = -1;

        public SimpleActionContainer(ButtonWidget button) {
            this.button = button;
        }

        public void setKeyCode(int i) { //TODO: add configurable keybinds
            this.keyCode = i;
        }

        public void clearKeyCode() {
            this.keyCode = -1;
        }

        public boolean matchesKeyCode(int i) {
            if(this.keyCode == -1) return false;
            return this.keyCode == i;
        }
    }

    public ButtonWidget makeSimpleButtonDefault(Runnable action, String id) {
        return makeSimpleButton(action, Text.translatable("arachne.simpleAction." + id + ".short"), Tooltip.of(Text.translatable("arachne.simpleAction." + id)), 40);
    }

    public static class ToolContainer {
        public ToolType tool;
        ButtonWidget button;
        private int keyCode = -1;

        public ToolContainer(ToolType tool, EditorMainScreen mls) {
            this.tool = tool;
            this.button = makeSimpleButton(mls.getToolSwitchAction(this), Text.translatable("arachne.editTool." + tool.getId() + ".nameShort"), Tooltip.of(Text.translatable("arachne.editTool." + tool.getId() + ".name")), 40);
        }

        public void onSelect(EditorMainScreen mls) {
            this.button.active = false;
            this.tool.onSwitchTo(mls);
        }

        public void onDeselect(EditorMainScreen mls) {
            this.button.active = true;
        }

        public void setKeyCode(int i) { //TODO: add configurable keybinds
            this.keyCode = i;
        }

        public void clearKeyCode() {
            this.keyCode = -1;
        }

        public boolean matchesKeyCode(int i) {
            if(this.keyCode == -1) return false;
            return this.keyCode == i;
        }
    }

    public Runnable getToolSwitchAction(ToolContainer toolContainer) {
        return () -> this.switchToTool(toolContainer);
    }

    public static ButtonWidget makeSimpleButton(Runnable action, Text text, Tooltip tooltip, int width) {
        return ButtonWidget.builder(text, button -> action.run()).width(width).tooltip(tooltip).build();
    }

    public static void tickFromBlockEntity(BlockPos pos) {
        // tick the weave when the block entity gets ticked
        if(MinecraftClient.getInstance().currentScreen instanceof EditorMainScreen screen) {
            if(screen.getScreenHandler().getPosition().equals(pos)) {
                screen.tickWeave();
            }
        }
    }

    public void tickWeave() {
        TimerHolder.getInstance().push("sketching_table_screen");
        if(!this.timePaused) {
            TimerHolder.dualPush("physics");
            int STEPS = 8;
            double dt = 1/20f;
            this.phySys.tick(dt, STEPS);
            TimerHolder.dualPop();
        }
        TimerHolder.getInstance().pop();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // TODO: consider changing this
        if(this.timePaused) {
            this.phySys.storeLastPositions();
        }

        Vec3i bp = this.getScreenHandler().getPosition();
        this.originPos = new Vec3d(bp.getX() + 0.5, bp.getY() + 1.5, bp.getZ() + 0.5);

        MinecraftClient.getInstance().getProfiler().push("arachne");
        TimerHolder.dualPush("sketching_table_screen");

        TimerHolder.getInstance().push("ss1");
        updateScreenSpacePositions(mouseX, mouseY);
        TimerHolder.getInstance().pop();
        if(this.toolContainer != null && this.toolContainer.tool != null) {
            TimerHolder.getInstance().push("tool");
            this.toolContainer.tool.onTick(this);
            TimerHolder.getInstance().pop();
        }
        TimerHolder.getInstance().push("ss2");
        updateScreenSpacePositions(mouseX, mouseY); //TODO: don't do this twice maybe?
        TimerHolder.getInstance().pop();
        TimerHolder.getInstance().push("unn");
        updateNearestNode(mouseX, mouseY);
        TimerHolder.getInstance().pop();

        if(this.getFocused() instanceof ButtonWidget) { //TODO: consider not doing this
            this.setFocused(null);
        }
        TimerHolder.dualPush("main");
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

        TimerHolder.dualPop();

        TimerHolder.dualPop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        TimerHolder.dualPush("drawForeground");
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(-this.x, -this.y, 0);
        this.propertyListWidget.render(context, mouseX, mouseY, 0);

        if(this.toolContainer != null && this.toolContainer.tool instanceof SelectTool selectTool) {
            Highlight highlight = selectTool.getHighlight();
            if(highlight != null) {
                Vec3d min = highlight.getMinPos();
                Vec3d max = highlight.getMaxPos();
                int xMin = (int)((min.x + 1) * this.width/2f);
                int xMax = (int)((max.x + 1) * this.width/2f);
                int yMin = (int)((-max.y + 1) * this.height/2f);
                int yMax = (int)((-min.y + 1) * this.height/2f);
                if(xMax - xMin > 1 && yMax - yMin > 1) {
                    context.drawBorder(xMin, yMin, xMax - xMin, yMax - yMin, 0xFFFFFFFF);
                    context.fill(xMin, yMin, xMax, yMax, 0x3FFFFFFF);
                }
            }
        }

        ArrayList<String> debugText = new ArrayList<>();
        if(this.showDebug) {
            debugText.add("FPS: " + MinecraftClient.getInstance().getCurrentFps());
            debugText.add("Origin: " + this.originPos.toString());
        }
        int selectedNodes = this.selection != null ? this.selection.getNodes().size() : 0;
        debugText.add("Nodes: " + this.phySys.nodes.size() + " (" + selectedNodes + " selected)");
        debugText.add("Links: " + this.phySys.links.size());
        debugText.add("Faces: " + this.phySys.faces.size());
        debugText.add("Render Materials: " + this.phySys.renderMaterials.size());
        if(this.showDebug) {
            debugText.add("Total Time Spent Per Frame (μs) : ");
        }

        int i = 0;
        for(String string : debugText) {
            context.drawText(this.textRenderer, string, 5, 5 + i, 0xFFFFFF, false);
            i += 10;
        }
        if(this.showDebug) {
            for (TimerTreeNode timer : TimerHolder.getInstance().getTimerTreeNodes()) {
                if (timer.getDepth() == 0) continue;
                int time = (int) timer.getTimer().getAverageElapsedTimeMicro();
                int color;
                if (time > 7500) { // more than 7.5ms
                    color = 0xFF0000;
                } else if (time > 2500) { // more than 2ms
                    color = 0xFF7F00;
                } else if (time > 1000) { // more than 1ms
                    color = 0xFFFF00;
                } else if (time > 250) { // more than 0.25ms
                    color = 0x7FFF00;
                } else if (time > 50) { // more than 0.05ms
                    color = 0x00FF00;
                } else { // less than 0.05ms
                    color = 0x00FFFF;
                }
                context.drawText(this.textRenderer, timer.getName(), 5 + 5 * timer.getDepth(), 5 + i, color, false);
                context.drawText(this.textRenderer, String.valueOf(time), 155 + 5 * timer.getDepth(), 5 + i, color, false);
                i += 10;
            }
        }
        matrices.pop();
        TimerHolder.dualPop();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        TimerHolder.dualPush("drawBackground");
        //super.renderBackground(context);
        renderNodes(context);
        TimerHolder.dualPop();
    }

    public void renderNodes(DrawContext context) {
        if(true) return;
        for (Node node : this.phySys.nodes) {
            if(!node.isVisible()) continue;

            Item item = Items.SNOWBALL;
            if(this.toolContainer != null && this.toolContainer.tool instanceof SelectTool selectTool && selectTool.getHighlight() != null) {
                Vec3d min = selectTool.getHighlight().getMinPos();
                Vec3d max = selectTool.getHighlight().getMaxPos();
                if(min.x <= node.posScreenSpace.x && node.posScreenSpace.x <= max.x) {
                    if(min.y <= node.posScreenSpace.y && node.posScreenSpace.y <= max.y) {
                        item = Items.SLIME_BALL;
                    }
                }
            } else {
                if (node == highlightedNode) {
                    item = Items.SLIME_BALL;
                }
            }
            if (this.toolContainer != null && this.toolContainer.tool instanceof FaceCreationTool fct) { //TODO: tidy
                if(fct.nodes.contains(node)) {
                    item = Items.REDSTONE;
                }
                if(!fct.nodes.isEmpty() && node == fct.nodes.get(0)) {
                    item = Items.GLOWSTONE_DUST;
                    if (node == highlightedNode && fct.nodes.size() >= 3) {
                        item = Items.SLIME_BALL;
                    }
                }
            }
            if (this.selectionMode == SelectionMode.VERTEX && this.selection.contains(node)) {
                item = Items.ENDER_PEARL;
            }
            if(!renderNodes && item == Items.SNOWBALL) continue;
            context.drawItem(new ItemStack(item), (int) ((node.posScreenSpace.x + 1) / 2 * this.width) - 8, (int) ((-node.posScreenSpace.y + 1) / 2 * this.height) - 8);
        }
    }

    public void updateNearestNode(int mouseX, int mouseY) {
        float mxss = mouseX * 2f / this.width - 1;
        float myss = -(mouseY * 2f / this.height - 1);

        float boundaryDistSqr = 0.0002f; // TODO: is this in the right coord space? might get wacky with small screens

        ArrayList<Node> highlightedNodes = new ArrayList<>();
        for(Node node : this.phySys.nodes) {
            double dx = mxss - node.posScreenSpace.x;
            double dy = myss - node.posScreenSpace.y;
            if(node.isVisible() && (dx * dx + dy * dy < boundaryDistSqr)) {
                highlightedNodes.add(node);
            }
        }

        double minDistance = Double.POSITIVE_INFINITY;
        highlightedNode = null;
        for(Node node : highlightedNodes) {
            double dx = mxss - node.posScreenSpace.x;
            double dy = myss - node.posScreenSpace.y;
            double dz = 1 - node.posScreenSpace.z;
            double distance = dx*dx + dy*dy + dz*dz;
            if(node.isVisible() && distance < minDistance) {
                minDistance = distance;
                highlightedNode = node;
            }
        }
    }

    public void updateScreenSpacePositions(int mouseX, int mouseY) {
        double fov = CameraController.getInstance().getFOV(70); //TODO: fix?
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        MatrixStack matrices1 = new MatrixStack();
        matrices1.multiplyPositionMatrix(MinecraftClient.getInstance().gameRenderer.getBasicProjectionMatrix(fov));
        Matrix4f projectionMatrix = matrices1.peek().getPositionMatrix();

        MatrixStack matrices2 = new MatrixStack();
        matrices2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices2.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        matrices2.translate(this.originPos.x, this.originPos.y, this.originPos.z);
        Matrix4f viewMatrix = matrices2.peek().getPositionMatrix();

        for(Node node : this.phySys.nodes) {
            node.updatePosScreenSpace(projectionMatrix, viewMatrix);
        }

        float mxss = mouseX * 2f / this.width - 1; //TODO: should this.width be the screen width instead? in this case they are the same but is that always true? would i need to offset mouse position too then?
        float myss = -(mouseY * 2f / this.height - 1);
        this.lastScreenSpaceMouseX = mxss;
        this.lastScreenSpaceMouseY = myss;

        Vector4f mousePos = new Vector4f(mxss, myss, 0, 1);
        Vector4f projPos = new Vector4f(mxss, myss, 1, 1);
        projectionMatrix = projectionMatrix.invert();
        mousePos = mousePos.mul(projectionMatrix);
        projPos = projPos.mul(projectionMatrix);
        if(mousePos.w != 0) {
            mousePos = mousePos.div(mousePos.w);
        }
        if(projPos.w != 0) {
            projPos = projPos.div(projPos.w);
        }
        viewMatrix = viewMatrix.invert();
        mousePos = mousePos.mul(viewMatrix);
        projPos = projPos.mul(viewMatrix);
        Vec3d mousePosProjected = new Vec3d(mousePos.x, mousePos.y, mousePos.z);
        Vec3d mouseLookProjected = new Vec3d(projPos.x - mousePos.x, projPos.y - mousePos.y, projPos.z - mousePos.z);
        mouseLookProjected = mouseLookProjected.normalize();
        this.mouseRay = new Line(mousePosProjected, mouseLookProjected);
        this.mouseRayWorld = new Line(mousePosProjected.add(this.originPos), mouseLookProjected);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.propertyListWidget != null && this.propertyListWidget.mouseClicked(mouseX, mouseY, button)) {
            if(this.getFocused() != this.propertyListWidget) {
                this.setFocused(this.propertyListWidget);
            }
            return true;
        }

        this.setFocused(null);

        // handle input for buttons and only buttons, other elements (if they exist) come later
        for (Element element : this.children()) {
            if (!element.mouseClicked(mouseX, mouseY, button)) continue;
            if (!(element instanceof ButtonWidget)) continue;
            this.setFocused(element);
            if (button == 0) {
                this.setDragging(true);
            }
            return true;
        }

        if(this.toolContainer != null && this.toolContainer.tool != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.toolContainer.tool.onClick(this);
            return true; // TODO: make work properly
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(this.toolContainer != null && this.toolContainer.tool != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.toolContainer.tool.onRelease(this);
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
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
        if(this.propertyListWidget != null && this.propertyListWidget.isHovered() && this.propertyListWidget.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }

        CameraController.getInstance().mouseScrolled(amount);

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_A && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            // ctrl + a
            this.selection.clear();
            switch(this.selectionMode) {
                case VERTEX -> {
                    for(Node node : this.phySys.nodes) {
                        this.selection.addNode(node);
                    }
                }
                case EDGE -> {
                    for(Link edge : this.phySys.links) {
                        this.selection.addEdge(edge);
                    }
                }
                case FACE -> {
                    for(Face face : this.phySys.faces) {
                        this.selection.addFace(face);
                    }
                }
            }
            return true;
        }

        if(keyCode == GLFW.GLFW_KEY_F3) {
            this.showDebug = !this.showDebug;
            return true;
        }

        if(this.propertyListWidget != null) {
            if(this.propertyListWidget.keyPressed(keyCode, scanCode, modifiers)) {
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

        if(keyCode == GLFW.GLFW_KEY_B) {
            if(this.selection != null) {
                for (Node node : this.selection.getNodes()) {
                    node.isStatic = !node.isStatic;
                }
            }
            return true;
        }

        for(SimpleActionContainer simpleActionContainer : simpleActionContainers) {
            if(simpleActionContainer.matchesKeyCode(keyCode)) {
                simpleActionContainer.button.onPress();
                return true;
            }
        }

        for(ToolContainer toolContainer : toolContainers) { //TODO: consider optimising? probably unnecessary
            if(toolContainer.matchesKeyCode(keyCode)) {
                switchToTool(toolContainer);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
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
        return super.keyReleased(keyCode, scanCode, modifiers); //TODO: should i be returning this
    }

    public void switchToTool(ToolContainer toolContainer) {
        if(this.toolContainer != null) {
            this.toolContainer.onDeselect(this);
        }
        this.toolContainer = toolContainer;
        toolContainer.onSelect(this);
    }

    public void addNode() {
        Vec3d node = CameraController.getInstance().targetPos.subtract(this.originPos);
        Node newNode = new Node(node);
        this.phySys.addNode(newNode);
        // TODO: change this later?
        // TODO: spawn node at mouse position and enable drag mode? or something??
        // TODO: only do when in link tool? or else make work correctly for face tool
        if(this.selection != null && !this.selection.getNodes().isEmpty()) {
            double x = 0;
            double y = 0;
            double z = 0;
            int n = this.selection.getNodes().size();
            for(Node selNode : this.selection.getNodes()) {
                this.phySys.links.add(new StringLink(selNode, newNode));
                x += selNode.pos.x;
                y += selNode.pos.y;
                z += selNode.pos.z;
            }
            newNode.pos = new Vec3d(x / n, y / n, z / n);
            newNode.isStatic = false;
            this.selection.clear();
            this.selection.addNode(newNode);
            /*
            for(ToolContainer toolContainer : toolContainers) { // TODO: bad
                if(toolContainer.tool instanceof DragTool) {
                    switchToTool(toolContainer);
                }
            }

             */
        }
    }

    public void toggleAltCameraMode() {
        CameraController.getInstance().USING_ALT_CAMERA_MODE = !CameraController.getInstance().USING_ALT_CAMERA_MODE;
    }

    public void toggleTime() {
        this.timePaused = !this.timePaused;
        if(this.timePaused) {
            this.phySys.storeLastPositions();
        }
    }

    public void toggleWind() {
        this.phySys.windActive = !this.phySys.windActive;
    }

    public void toggleNodeVisibility() {
        this.renderNodes = !this.renderNodes;
    }

    public void toggleLinkVisibility() {
        this.renderLinks = !this.renderLinks;
    }

    public void toggleFaceVisibility() {
        this.renderFaces = !this.renderFaces;
    }

    public void eraseSelectedNode() {
        if(this.selection == null) return;
        if(this.selectionMode != SelectionMode.VERTEX) return; // TODO: fix for other modes

        ArrayList<Link> delLinks = new ArrayList<>();
        for(Link link : this.phySys.links) {
            if(this.selection.contains(link.node1) || this.selection.contains(link.node2)) {
                delLinks.add(link);
            }
        }
        ArrayList<Face> delFaces = new ArrayList<>();
        for(Face face : this.phySys.faces) {
            boolean containsNode = false;
            for(Node node : face.nodes) {
                if(this.selectionMode == SelectionMode.VERTEX && this.selection.contains(node)) {
                    containsNode = true;
                    break;
                }
            }
            if(containsNode) {
                delFaces.add(face);
            }
        }
        for(Link link : delLinks) {
            this.phySys.links.remove(link);
        }
        for(Face face : delFaces) {
            this.phySys.faces.remove(face);
        }
        for(Node node : selection.getNodes()) {
            this.phySys.nodes.remove(node);
        }
        this.selection.clear();
    }

    public void splitLinkButBadly() {
        if(this.selection == null) return;

        ArrayList<Link> nodeLinks = new ArrayList<>();
        for(Link link : this.phySys.links) {
            if(this.selectionMode == SelectionMode.VERTEX && (this.selection.contains(link.node1) || this.selection.contains(link.node2))) {
                nodeLinks.add(link);
            }
        }
        if(nodeLinks.size() > 0) {
            ArrayList<Node> newNodes = new ArrayList<>();
            for(Link link : nodeLinks) {
                Node newNode = new Node(link.node1.pos.add(link.node2.pos).multiply(1/2f));
                this.phySys.addNode(newNode);
                newNodes.add(newNode);
                newNode.isStatic = false;
                this.phySys.links.add(new StringLink(newNode, link.node2));
                link.node2 = newNode;
            }
        }
    }

    public void subdivideAllFaces() {
        ArrayList<Face> newFaces = new ArrayList<>();
        for(Face face : this.phySys.faces) {
            Node newNode = new Node(face.getCenterPos());
            newNode.isStatic = false;
            this.phySys.addNode(newNode);
            for(Node node : face.nodes) {
                Link newLink = new StringLink(newNode, node);
                this.phySys.links.add(newLink);
            }
            for(int i = 0; i < face.nodes.length; i++) {
                Node node1 = face.nodes[i];
                Node node2 = face.nodes[(i+1)%face.nodes.length];
                ArrayList<Node> nl = new ArrayList<>();
                nl.add(node1);
                nl.add(node2);
                nl.add(newNode);
                Face newFace = new Face(nl);
                newFace.setColor(face.r, face.g, face.b);
                newFaces.add(newFace);
            }
        }
        this.phySys.faces.clear();
        this.phySys.faces.addAll(newFaces);
    }

    public void spawnGrid() {
        //Vec3d center = new Vec3d(this.getScreenHandler().getPosition().getX(), this.getScreenHandler().getPosition().getY(), this.getScreenHandler().getPosition().getZ());
        Vec3d center = Vec3d.ZERO;
        center = center.add(-0.5f, 0, -0.5f);
        int SUBDIVISIONS = 16;
        Node[][] nodeGrid = new Node[SUBDIVISIONS+1][SUBDIVISIONS+1];
        for(int x = 0; x <= SUBDIVISIONS; x++) {
            for(int z = 0; z <= SUBDIVISIONS; z++) {
                Node node = new Node(new Vec3d(center.x + x/(float)SUBDIVISIONS, center.y, center.z + z/(float)SUBDIVISIONS));
                if(x != 0 && x != SUBDIVISIONS && z != 0 && z != SUBDIVISIONS) {
                    node.isStatic = false;
                }
                this.phySys.addNode(node);
                nodeGrid[x][z] = node;
            }
        }
        for(int i = 0; i <= SUBDIVISIONS; i++) {
            for(int j = 0; j <= SUBDIVISIONS; j++) {
                if(i != SUBDIVISIONS) {
                    Link link = new StringLink(nodeGrid[i][j], nodeGrid[i+1][j]);
                    this.phySys.links.add(link);
                }
                if(j != SUBDIVISIONS) {
                    Link link = new StringLink(nodeGrid[i][j], nodeGrid[i][j+1]);
                    this.phySys.links.add(link);
                }
            }
        }
        for(int i = 0; i < SUBDIVISIONS; i++) {
            for(int j = 0; j < SUBDIVISIONS; j++) {
                ArrayList<Node> fl = new ArrayList<>();
                fl.add(nodeGrid[i][j]);
                fl.add(nodeGrid[i][j+1]);
                fl.add(nodeGrid[i+1][j+1]);
                fl.add(nodeGrid[i+1][j]);
                Face face = new Face(fl);
                // TODO: tweak
                Random random = new Random();
                int h = 0;
                for (Node node : fl) {
                    h += node.hashCode();
                }
                random.setSeed(h);
                face.r = random.nextInt(128) + 128;
                face.g = random.nextInt(128) + 128;
                face.b = random.nextInt(128) + 128;
                this.phySys.faces.add(face);
            }
        }
    }

    public void spawnCube() {
        double xm = -0.25;
        double xM = +0.25;
        double ym = -0.25;
        double yM = +0.25;
        double zm = -0.25;
        double zM = +0.25;
        double[] x = new double[]{xm, xM, xm, xM, xm, xM, xm, xM};
        double[] y = new double[]{ym, ym, yM, yM, ym, ym, yM, yM};
        double[] z = new double[]{zm, zm, zm, zm, zM, zM, zM, zM};

        Node[] nodes = new Node[8];
        for(int i = 0; i < 8; i++) {
            nodes[i] = new Node(new Vec3d(x[i], y[i], z[i]));
            this.phySys.addNode(nodes[i]);
        }

        // TODO: check all faces are defined counter-clockwise
        int[] indices = new int[]{0, 4, 6, 2, 1, 3, 7, 5, 0, 1, 5, 4, 2, 6, 7, 3, 0, 2, 3, 1, 4, 5, 7, 6};
        for(int i = 0; i < 6; i++) {
            ArrayList<Node> n = new ArrayList<>();
            n.add(nodes[indices[4 * i]]);
            n.add(nodes[indices[4 * i + 1]]);
            n.add(nodes[indices[4 * i + 2]]);
            n.add(nodes[indices[4 * i + 3]]);
            Face f = new Face(n);
            f.setColor(i/2==0 ? 0xFF : 0x7F, i/2==1 ? 0xFF : 0x7F, i/2==2? 0xFF : 0x7F);
            this.phySys.faces.add(f);
        }
    }
}
