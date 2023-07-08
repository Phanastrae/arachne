package phanastrae.arachne.block.blockentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Clearable;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.setup.ModBlockEntities;
import phanastrae.arachne.setup.ModItems;

import java.util.function.Consumer;

public class SketchingTableBlockEntity extends BlockEntity implements Clearable, SidedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final int[] rotations = new int[inventory.size()];
    public static final int ROTATION_MAX = 16;

    public SketchingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SKETCHING_TABLE, pos, state);
    }

    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    public int[] getRotations() {
        return this.rotations;
    }

    // this gets changed on clientside
    public static Consumer<BlockPos> TICK_EVENT;

    public static <T extends BlockEntity> void tick(World world, BlockPos pos, BlockState state, SketchingTableBlockEntity blockEntity) {
        if(TICK_EVENT != null) {
            TICK_EVENT.accept(pos);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory.clear();
        Inventories.readNbt(nbt, this.inventory);
        if (nbt.contains("Rotations", NbtElement.INT_ARRAY_TYPE)) {
            int[] is = nbt.getIntArray("Rotations");
            System.arraycopy(is, 0, this.rotations, 0, is.length);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory, true);
        nbt.putIntArray("Rotations", this.rotations);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        Inventories.writeNbt(nbtCompound, this.inventory, true);
        nbtCompound.putIntArray("Rotations", this.rotations);
        return nbtCompound;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    public boolean addItem(@Nullable Entity user, ItemStack stack) {
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack itemStack = this.inventory.get(i);
            if (!itemStack.isEmpty()) continue;
            if(user == null) {
                this.rotations[i] = 0;
            } else {
                int yaw = ((int)Math.round(user.getYaw() * ROTATION_MAX / 360)) % ROTATION_MAX;
                if(yaw < 0) yaw += ROTATION_MAX;
                this.rotations[i] = yaw;
            }
            this.inventory.set(i, stack.split(1));
            this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(user, this.getCachedState()));
            this.updateListeners();
            return true;
        }
        return false;
    }

    public void dropItems(Entity user) {
        if(world == null) return;
        // remove empty sketches if in creative
        if(user instanceof PlayerEntity player && player.getAbilities().creativeMode) {
            for(int i = 0; i < this.getItems().size(); i++) {
                if(this.getItems().get(i).isOf(ModItems.SKETCH)) {
                    this.getItems().set(i, ItemStack.EMPTY);
                }
            }
        }
        ItemScatterer.spawn(world, pos.up(), this.getItems());
        this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(user, this.getCachedState()));
        this.updateListeners();
    }

    public boolean canAccept(ItemStack itemStack) {
        return itemStack != null && (itemStack.isOf(ModItems.SKETCH) || itemStack.isOf(ModItems.FILLED_SKETCH));
    }

    private void updateListeners() {
        this.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return this.getItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.getItems().get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.getItems(), slot, amount);
        if (!itemStack.isEmpty()) {
            this.updateListeners();
        }
        return itemStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        this.updateListeners();
        return Inventories.removeStack(this.getItems(), slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.getItems().set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
        this.updateListeners();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return this.canAccept(stack);
    }

    @Override
    public int getMaxCountPerStack() {
        return 1; // TODO: this doesn't do anything to hoppers?
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[1];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.getCount() <= 1;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
