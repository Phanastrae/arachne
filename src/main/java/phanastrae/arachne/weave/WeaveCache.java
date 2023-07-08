package phanastrae.arachne.weave;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class WeaveCache {

    @Nullable
    private Weave WEAVE_LAST;
    @Nullable
    private NbtCompound NBT_LAST;

    @Nullable
    public Weave getOrMakeWeave(@Nullable NbtCompound nbt, Function<NbtCompound, Weave> function) {
        if (nbt != NBT_LAST) {
            setWeave(nbt, function);
        }
        return WEAVE_LAST;
    }

    private void setWeave(NbtCompound nbtSketch, Function<NbtCompound, Weave> function) {
        if(nbtSketch == null) {
            this.WEAVE_LAST = null;
        } else {
            this.WEAVE_LAST = function.apply(nbtSketch);
        }
        this.NBT_LAST = nbtSketch;
    }

    @Nullable
    public Weave getWeave() {
        return this.WEAVE_LAST;
    }

    // TODO: use
    public void clearWeave() {
        this.WEAVE_LAST = null;
        this.NBT_LAST = null;
    }

    public void tick(double dt, int steps) {
        if(steps < 1) return;
        if(this.WEAVE_LAST instanceof WeaveTickable weaveTickable) {
            weaveTickable.tick(dt, steps);
        }
    }

    public void tick(double dt) {
        this.tick(dt, 1);
    }


    public static NbtCompound getNbtSketch(NbtCompound nbt) {
        if(nbt == null || nbt.isEmpty()) {
            return null;
        } else {
            NbtCompound nbtSketch = nbt.getCompound("sketchData");
            if(nbtSketch != null && nbtSketch.isEmpty()) {
                return null;
            } else {
                return nbtSketch;
            }
        }
    }


    public static NbtCompound getNbtWeave(NbtCompound nbt) {
        if(nbt == null || nbt.isEmpty()) {
            return null;
        } else {
            NbtCompound nbtWeave = nbt.getCompound("weaveData");
            if(nbtWeave != null && nbtWeave.isEmpty()) {
                return null;
            } else {
                return nbtWeave;
            }
        }
    }
}
