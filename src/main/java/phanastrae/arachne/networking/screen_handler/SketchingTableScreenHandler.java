package phanastrae.arachne.networking.screen_handler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import phanastrae.arachne.setup.ModScreenHandlerTypes;
import phanastrae.arachne.util.TableMultiblock;

public class SketchingTableScreenHandler extends ScreenHandler {

    private final PlayerInventory playerInventory;
    private final ScreenHandlerContext context;
    private final PropertyDelegate markerPosition;
    private final Inventory inventory;
    private TableMultiblock tableMultiblock;

    public SketchingTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1), ScreenHandlerContext.EMPTY, new ArrayPropertyDelegate(3));
    }

    public SketchingTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, ScreenHandlerContext context, PropertyDelegate markerPosition) {
        super(ModScreenHandlerTypes.SKETCHING_TABLE, syncId);
        this.playerInventory = playerInventory;
        this.context = context;
        this.inventory = inventory;
        this.markerPosition = markerPosition;
        this.addSlot(new SketchSlot(inventory, 0, 0, 0));
        addProperties(markerPosition);
    }

    public PlayerInventory getPlayerInventory() {
        return this.playerInventory;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Vec3i getPosition() {
        return new Vec3i(markerPosition.get(0), markerPosition.get(1), markerPosition.get(2));
    }

    public TableMultiblock getTableMultiblock(World world) {
        BlockPos bp = new BlockPos(getPosition());
        if(this.tableMultiblock == null || !this.tableMultiblock.isCenter(bp)) {
            this.tableMultiblock = new TableMultiblock(bp);
            this.tableMultiblock.init(world);
        }
        return tableMultiblock;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
        //return canUse(this.context, player, ModBlocks.SKETCHING_TABLE);
    }

    @Override
    public void updateToClient() {
        super.updateToClient();
    }
}
