package phanastrae.arachne.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.setup.ModEntities;
import phanastrae.arachne.setup.ModItems;

public class WeaveEntity extends Entity {

    static final TrackedData<ItemStack> WEAVE_ITEM_STACK = DataTracker.registerData(WeaveEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    public WeaveEntity(EntityType<? extends Entity> type, World world) {
        super(type, world);
    }

    public WeaveEntity(World world, double x, double y, double z) {
        this(ModEntities.WEAVE_ENTITY, world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    public void setItemStack(ItemStack itemStack) {
        this.dataTracker.set(WEAVE_ITEM_STACK, itemStack);
    }

    public ItemStack getItemStack() {
        return this.dataTracker.get(WEAVE_ITEM_STACK);
    }

    @Nullable
    public NbtCompound getWeaveNbt() {
        ItemStack itemStack = this.getItemStack();
        if(itemStack == null || itemStack.isEmpty()) return null;
        NbtCompound nbt = itemStack.getNbt();
        if(nbt != null) {
            nbt = nbt.getCompound("weaveData");
            if(nbt.isEmpty()) {
                nbt = null;
            }
        }
        return nbt;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(WEAVE_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        NbtCompound itemNbt = nbt.getCompound("weaveItem");
        ItemStack itemStack;
        if(itemNbt != null && !itemNbt.isEmpty()) {
            itemStack = ItemStack.fromNbt(itemNbt);
        } else {
            itemStack = ItemStack.EMPTY;
        }
        this.setItemStack(itemStack);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        ItemStack itemStack = this.getItemStack();
        if(itemStack != null) {
            NbtCompound itemNbt = new NbtCompound();
            itemStack.writeNbt(itemNbt);
            nbt.put("weaveItem", itemNbt);
        }
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity entity = source.getSource();
        if(entity instanceof PlayerEntity player) {
            if(player.getAbilities().allowModifyWorld && source.isOf(DamageTypes.PLAYER_ATTACK)) { // TODO: consider changing how this works? add invulnerable toggle?
                this.discard();
                // drop item unless player is in creative mode and already holding it
                ItemStack itemStack = this.getItemStack();
                boolean dropItem = true;
                if(player.getAbilities().creativeMode) {
                    ItemStack mainStack = player.getMainHandStack();
                    ItemStack offStack = player.getOffHandStack();
                    if(ItemStack.canCombine(itemStack, mainStack) || ItemStack.canCombine(itemStack, offStack)) {
                        dropItem = false;
                    }
                }
                if(dropItem) {
                    if (itemStack != null) {
                        this.dropStack(itemStack);
                    }
                }
                return true;
            }
        }
        return super.damage(source, amount);
    }

    // TODO: culling
    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;
    }

    @Override
    public void kill() {
        super.kill();
    }

    @Nullable
    @Override
    public ItemStack getPickBlockStack() {
        return this.getItemStack();
    }
}
