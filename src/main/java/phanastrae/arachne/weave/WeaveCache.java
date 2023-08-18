package phanastrae.arachne.weave;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;

import java.util.function.Function;

public class WeaveCache {

    @Nullable
    private WeaveInstance WEAVE_LAST;
    @Nullable
    private NbtCompound NBT_LAST;

    @Nullable
    public WeaveInstance getOrMakeWeave(@Nullable NbtCompound nbt) {
        if (nbt != NBT_LAST) {
            setWeave(nbt);
        }
        return WEAVE_LAST;
    }

    private void setWeave(NbtCompound nbtSketch) {
        if(nbtSketch == null) {
            this.WEAVE_LAST = null;
        } else {
            SketchWeave sketchWeave = NBTSerialization.readSketchWeave(nbtSketch);
            if(sketchWeave == null) {
                this.WEAVE_LAST = null;
            } else {
                this.WEAVE_LAST = new WeaveInstance(sketchWeave.buildWeave());
            }
        }
        this.NBT_LAST = nbtSketch;
    }

    @Nullable
    public WeaveInstance getWeave() {
        return this.WEAVE_LAST;
    }

    // TODO: use
    public void clearWeave() {
        this.WEAVE_LAST = null;
        this.NBT_LAST = null;
    }

    public void update(World world) {
        if(this.WEAVE_LAST != null) {
            this.WEAVE_LAST.waitForUpdate();
            this.WEAVE_LAST.lock();
            this.WEAVE_LAST.preUpdate(world);
            this.WEAVE_LAST.setUpdating();
            this.WEAVE_LAST.unlock();
            Arachne.runnableQueue.queue(this.WEAVE_LAST::update);
        }
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
