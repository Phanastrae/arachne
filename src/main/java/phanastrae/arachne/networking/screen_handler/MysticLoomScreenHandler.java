package phanastrae.arachne.networking.screen_handler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import phanastrae.arachne.setup.ModBlocks;
import phanastrae.arachne.setup.ModItems;
import phanastrae.arachne.setup.ModScreenHandlerTypes;

public class MysticLoomScreenHandler extends ScreenHandler {

    private final ScreenHandlerContext context;
    Runnable inventoryChangeListener = () -> {};
    private final Slot materialSlot;
    final Slot sketchSlot;
    private final Slot outputSlot;
    long lastTakeResultTime;
    private final Inventory input = new SimpleInventory(2){
        @Override
        public void markDirty() {
            super.markDirty();
            MysticLoomScreenHandler.this.onContentChanged(this);
            MysticLoomScreenHandler.this.inventoryChangeListener.run();
        }
    };
    private final Inventory output = new SimpleInventory(1){
        @Override
        public void markDirty() {
            super.markDirty();
            MysticLoomScreenHandler.this.inventoryChangeListener.run();
        }
    };

    public MysticLoomScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public MysticLoomScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModScreenHandlerTypes.MYSTIC_LOOM, syncId);
        this.context = context;
        this.materialSlot = this.addSlot(new Slot(this.input, 0, 26, 33){
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.STRING);
            }
        });
        this.sketchSlot = this.addSlot(new Slot(this.input, 1, 80, 8){
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.FILLED_SKETCH) && stack.getNbt() != null && stack.getNbt().contains("sketchData", NbtElement.COMPOUND_TYPE);
            }
        });
        this.outputSlot = this.addSlot(new Slot(this.output, 0, 80, 58){
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                MysticLoomScreenHandler.this.materialSlot.takeStack(1);
                context.run((world, pos) -> {
                    long l = world.getTime();
                    if (MysticLoomScreenHandler.this.lastTakeResultTime != l) {
                        // TODO tweak sound
                        world.playSound(null, pos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        MysticLoomScreenHandler.this.lastTakeResultTime = l;
                    }
                });
                super.onTakeItem(player, stack);
            }
        });
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return MysticLoomScreenHandler.canUse(this.context, player, ModBlocks.MYSTIC_LOOM);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        ItemStack materialItemStack = this.materialSlot.getStack();
        ItemStack sketchItemStack = this.sketchSlot.getStack();

        ItemStack outputItemStack = ItemStack.EMPTY;
        if (!sketchItemStack.isEmpty() && !materialItemStack.isEmpty()) {
            NbtCompound nbt = sketchItemStack.getNbt();
            if(nbt != null) {
                nbt = nbt.getCompound("sketchData");
                if(nbt.isEmpty()) {
                    nbt = null;
                }
            }
            if(nbt != null) {
                outputItemStack = new ItemStack(ModItems.WEAVE, 1);
                NbtCompound outputNbt = outputItemStack.getOrCreateNbt();
                outputNbt.put("weaveData", nbt);
            }
        }
        if (!ItemStack.areEqual(outputItemStack, this.outputSlot.getStack())) {
            this.outputSlot.setStackNoCallbacks(outputItemStack);
            this.sendContentUpdates();
        }
    }

    public void setInventoryChangeListener(Runnable inventoryChangeListener) {
        this.inventoryChangeListener = inventoryChangeListener;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotID) {
        Slot slot = this.slots.get(slotID);
        if(slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        int INVENTORY_START = 3;
        int INVENTORY_HOTBAR = 30;
        int INVENTORY_END = 39;

        ItemStack slotStack = slot.getStack();
        ItemStack itemStack = slotStack.copy();
        if (slotID == this.outputSlot.id) {
            if (!this.insertItem(slotStack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(slotStack, itemStack);
        } else if(slotID == this.materialSlot.id || slotID == this.sketchSlot.id) {
            if(!this.insertItem(slotStack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if((slotStack.isOf(Items.STRING))) {
            if(!this.insertItem(slotStack, this.materialSlot.id, this.materialSlot.id + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if((slotStack.isOf(ModItems.FILLED_SKETCH))) {
            if(!this.insertItem(slotStack, this.sketchSlot.id, this.sketchSlot.id + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if((slotID >= INVENTORY_START && slotID < INVENTORY_HOTBAR)) {
            if(!this.insertItem(slotStack, INVENTORY_HOTBAR, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if(slotID >= INVENTORY_HOTBAR && slotID < INVENTORY_END && !this.insertItem(slotStack, INVENTORY_START, INVENTORY_HOTBAR, false)) {
            return ItemStack.EMPTY;
        }

        if (slotStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (slotStack.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, slotStack);

        return itemStack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    public Slot getMaterialSlot() {
        return materialSlot;
    }

    public Slot getSketchSlot() {
        return sketchSlot;
    }

    public Slot getOutputSlot() {
        return outputSlot;
    }
}
