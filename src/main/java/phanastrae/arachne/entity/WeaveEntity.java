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
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.item.WeaveControlItem;
import phanastrae.arachne.setup.ModEntities;
import phanastrae.arachne.setup.ModItems;
import phanastrae.arachne.weave.WeaveControl;
import phanastrae.arachne.weave.WeaveInstance;

public class WeaveEntity extends Entity {

    static final TrackedData<ItemStack> WEAVE_ITEM_STACK = DataTracker.registerData(WeaveEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    static final TrackedData<NbtCompound> WEAVE_EXTRA_NBT = DataTracker.registerData(WeaveEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

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

    @Override
    public void tick() {
        super.tick();
        NbtCompound nbt = this.getExtraNbt();
        int i = nbt.getInt("direction");
        WeaveControl.forEachWeaveInEntity(this, (string, weaveCache) -> {
            if(weaveCache != null) {
                WeaveInstance wi = weaveCache.getWeave();
                if(wi != null) {
                    wi.windRotation = i * 90;
                }
            }
        });
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        if(player != null && hand != null) {
            World world = player.getWorld();
            ItemStack stack = player.getStackInHand(hand);
            if(player.getItemCooldownManager().getCooldownProgress(ModItems.WEAVE_CONTROLLER, 0) == 0) {
                if (world != null && stack != null && !stack.isEmpty() && stack.getItem() instanceof WeaveControlItem) {
                    if (world.isClient) {
                        return ActionResult.SUCCESS;
                    } else {
                        player.getItemCooldownManager().set(ModItems.WEAVE_CONTROLLER, 10);
                        int j = cycleDirection();
                        String s = "";
                        switch(j) {
                            case 0 -> s = "West";
                            case 1 -> s = "South";
                            case 2 -> s = "East";
                            case 3 -> s = "North";
                        }
                        ((ServerPlayerEntity)player).sendMessageToClient(Text.of("Switch Wind Direction to " + s), true);
                        return ActionResult.CONSUME;
                    }
                }
            }
        }
        return super.interactAt(player, hitPos, hand);
    }

    public void setItemStack(ItemStack itemStack) {
        this.dataTracker.set(WEAVE_ITEM_STACK, itemStack);
    }

    public ItemStack getItemStack() {
        return this.dataTracker.get(WEAVE_ITEM_STACK);
    }

    public void setExtraNbt(NbtCompound nbt) {
        this.dataTracker.set(WEAVE_EXTRA_NBT, nbt);
    }

    public NbtCompound getExtraNbt() {
        return this.dataTracker.get(WEAVE_EXTRA_NBT);
    }

    public int cycleDirection() {
        NbtCompound nbt = this.getExtraNbt().copy();
        int i = nbt.getInt("direction");
        int j = (i+1)%4;
        nbt.putInt("direction", j);
        this.setExtraNbt(nbt);
        return j;
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
        this.dataTracker.startTracking(WEAVE_EXTRA_NBT, new NbtCompound());
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
        this.setExtraNbt(nbt.getCompound("extraData"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        ItemStack itemStack = this.getItemStack();
        if(itemStack != null) {
            NbtCompound itemNbt = new NbtCompound();
            itemStack.writeNbt(itemNbt);
            nbt.put("weaveItem", itemNbt);
        }
        nbt.put("extraData", this.getExtraNbt());
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
