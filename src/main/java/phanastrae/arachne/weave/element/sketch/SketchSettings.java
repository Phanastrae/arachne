package phanastrae.arachne.weave.element.sketch;

import net.minecraft.nbt.NbtCompound;
import phanastrae.arachne.weave.SketchWeave;
import phanastrae.arachne.weave.element.Serializable;

public class SketchSettings extends SketchElement implements Serializable {

    int stepMain = 3;
    int stepWind = 0;
    String name = "";
    double windMultiplier = 1;
    double gravityMultiplier = 1;
    boolean doCulling = true;
    double height = 5;
    double width = 5;

    public void setStepMain(int i) {
        if(i < 0) i = -1;
        if(i > 5) i = 5;
        this.stepMain = i;
    }

    public void setStepWind(int i) {
        if(i < 0) i = -1;
        if(i > 5) i = 5;
        this.stepWind = i;
    }

    public int getStepMain() {
        return this.stepMain;
    }

    public int getStepWind() {
        return this.stepWind;
    }

    public int getActualStepMain() {
        int i = this.stepMain;
        if(i < 0) return 0;
        if(i > 5) i = 5;
        return 1 << i;
    }

    public int getActualStepWind() {
        int i = this.stepWind;
        if(i < 0) return 0;
        if(i > 5) i = 5;
        return 1 << i;
    }

    public double getGravityMultiplier() {
        return this.gravityMultiplier;
    }

    public void setGravityMultiplier(double d) {
        this.gravityMultiplier = d;
    }

    public double getWindMultiplier() {
        return this.windMultiplier;
    }

    public void setWindMultiplier(double d) {
        this.windMultiplier = d;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean getDoCulling() {
        return this.doCulling;
    }

    public void setDoCulling(boolean b) {
        this.doCulling = b;
    }

    public double getWidth() {
        return this.width;
    }

    public void setWidth(double d) {
        if(d >= 0) {
            this.width = d;
        } else {
            this.width = 0;
        }
    }

    public double getHeight() {
        return this.height;
    }

    public void setHeight(double d) {
        if(d >= 0) {
            this.height = d;
        } else {
            this.height = 0;
        }
    }

    @Override
    public void read(NbtCompound nbt, SketchWeave sketchWeave) {
        if(nbt.contains("stepMain", NbtCompound.INT_TYPE)) {
            this.stepMain = nbt.getInt("stepMain");
        }
        if(nbt.contains("stepWind", NbtCompound.INT_TYPE)) {
            this.stepWind = nbt.getInt("stepWind");
        }
        if(nbt.contains("name", NbtCompound.STRING_TYPE)) {
            this.name = nbt.getString("name");
        }
        if(nbt.contains("windMultiplier", NbtCompound.DOUBLE_TYPE)) {
            this.windMultiplier = nbt.getDouble("windMultiplier");
        }
        if(nbt.contains("gravityMultiplier", NbtCompound.DOUBLE_TYPE)) {
            this.gravityMultiplier = nbt.getDouble("gravityMultiplier");
        }
        if(nbt.contains("doCulling")) {
            this.doCulling = nbt.getBoolean("doCulling");
        }
        if(nbt.contains("height", NbtCompound.DOUBLE_TYPE)) {
            this.height = nbt.getDouble("height");
        }
        if(nbt.contains("width", NbtCompound.DOUBLE_TYPE)) {
            this.width = nbt.getDouble("width");
        }
    }

    @Override
    public NbtCompound write() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("stepMain", stepMain);
        nbt.putInt("stepWind", stepWind);
        nbt.putString("name", name);
        nbt.putDouble("windMultiplier", windMultiplier);
        nbt.putDouble("gravityMultiplier", gravityMultiplier);
        nbt.putBoolean("doCulling", doCulling);
        nbt.putDouble("height", height);
        nbt.putDouble("width", width);
        return nbt;
    }
}
