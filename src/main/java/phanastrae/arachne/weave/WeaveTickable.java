package phanastrae.arachne.weave;

import net.minecraft.nbt.NbtCompound;

public abstract class WeaveTickable extends Weave {

    public WeaveTickable() {
        super();
    }

    public WeaveTickable(NbtCompound nbt) {
        super(nbt);
    }

    public abstract void storeLastPositions();

    public abstract void tick(double dt);
    public abstract void tick(double dt, int steps);
}
