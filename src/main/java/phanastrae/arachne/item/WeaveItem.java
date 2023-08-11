package phanastrae.arachne.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.entity.WeaveEntity;
import phanastrae.arachne.setup.ModItems;

import java.util.List;
import java.util.Optional;

public class WeaveItem extends Item {
    public WeaveItem(Settings settings) {
        super(settings);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        SketchTooltipData sttd = new SketchTooltipData(stack.getNbt(), SketchTooltipData.WeaveDataType.WEAVE);
        return Optional.of(sttd);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if(nbt != null) {
            if(nbt.contains("weaveData", NbtElement.COMPOUND_TYPE)) {
                NbtCompound nbt2 = nbt.getCompound("weaveData");
                if(nbt2.contains("fileId", NbtElement.STRING_TYPE)) {
                    String fileId = nbt2.getString("fileId");
                    tooltip.add(Text.of(fileId).copy().formatted(Formatting.LIGHT_PURPLE));
                }
            }
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if(nbt != null) {
            NbtCompound nbt2 = nbt.getCompound("weaveData");
            if(!nbt2.isEmpty()) {
                NbtCompound nbt3 = nbt2.getCompound("settings");
                if(nbt3.contains("name", NbtCompound.STRING_TYPE)) {
                    String name = nbt3.getString("name");
                    if(name != null && name.length() > 0) {
                        return Text.translatable("item.arachne.weave.named", Text.of(name).copy().formatted(Formatting.ITALIC));
                    }
                }
            }
        }
        return super.getName(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // TODO: tidy; move to dedicated item
        ItemStack itemStack = context.getStack();
        if(itemStack == null || itemStack.isEmpty() || !itemStack.isOf(ModItems.WEAVE)) {
            return ActionResult.PASS;
        }
        World world = context.getWorld();
        if(world == null) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            // TODO: add option for non-centered placement?
            BlockPos bp = context.getBlockPos();
            if (!world.getBlockState(context.getBlockPos()).isReplaceable()) {
                bp = bp.add(context.getSide().getVector());
            }
            Vec3d pos = bp.toCenterPos();

            WeaveEntity weaveEntity = new WeaveEntity(world, pos.getX(), pos.getY() - 0.5, pos.getZ());
            weaveEntity.setItemStack(context.getStack().copyWithCount(1));
            if(!(context.getPlayer() != null && context.getPlayer().getAbilities().creativeMode)) {
                context.getStack().decrement(1);
            }
            world.spawnEntity(weaveEntity);
        } else {
            world.playSound(context.getHitPos().x, context.getHitPos().y, context.getHitPos().z, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1f, 1f, false);
        }
        return ActionResult.SUCCESS;
    }
}
