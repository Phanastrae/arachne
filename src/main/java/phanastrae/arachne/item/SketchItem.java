package phanastrae.arachne.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.entity.WeaveEntity;
import phanastrae.arachne.setup.ModItems;

import java.util.List;
import java.util.Optional;

public class SketchItem extends Item {
    public SketchItem(Settings settings) {
        super(settings);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        SketchTooltipData sttd = new SketchTooltipData(stack.getNbt(), SketchTooltipData.WeaveDataType.SKETCH);
        return Optional.of(sttd);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("arachne.tooltip.sketch_sketch", Text.translatable("block.arachne.sketching_table")).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("arachne.tooltip.sketch_weave", Text.translatable("block.arachne.mystic_loom")).formatted(Formatting.GRAY));
        NbtCompound nbt = stack.getNbt();
        if(nbt != null) {
            if(nbt.contains("sketchData", NbtElement.COMPOUND_TYPE)) {
                NbtCompound nbt2 = nbt.getCompound("sketchData");
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
            NbtCompound nbt2 = nbt.getCompound("sketchData");
            if(!nbt2.isEmpty()) {
                NbtCompound nbt3 = nbt2.getCompound("settings");
                if(nbt3.contains("name", NbtCompound.STRING_TYPE)) {
                    String name = nbt3.getString("name");
                    if(name != null && name.length() > 0) {
                        return Text.translatable("item.arachne.sketch.named", Text.of(name).copy().formatted(Formatting.ITALIC));
                    }
                }
            }
        }
        return super.getName(stack);
    }
}
