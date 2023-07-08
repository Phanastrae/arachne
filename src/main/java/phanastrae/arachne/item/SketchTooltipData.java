package phanastrae.arachne.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class SketchTooltipData implements TooltipData {

    @Nullable
    private final NbtCompound nbt;
    private final WeaveDataType weaveDataType;

    public enum WeaveDataType {
        SKETCH,
        WEAVE
    }

    public SketchTooltipData(@Nullable NbtCompound nbt, WeaveDataType weaveDataType) {
        this.nbt = nbt;
        this.weaveDataType = weaveDataType;
    }

    @Nullable
    public NbtCompound getNbt() {
        return this.nbt;
    }

    public WeaveDataType getWeaveDataType() {
        return this.weaveDataType;
    }
}
