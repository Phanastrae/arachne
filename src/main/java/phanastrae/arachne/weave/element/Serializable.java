package phanastrae.arachne.weave.element;

import net.minecraft.nbt.NbtCompound;
import phanastrae.arachne.weave.SketchWeave;

public interface Serializable {
    void read(NbtCompound nbt, SketchWeave sketchWeave);
    NbtCompound write();
}
