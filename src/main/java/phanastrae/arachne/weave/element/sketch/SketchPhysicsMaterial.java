package phanastrae.arachne.weave.element.sketch;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import phanastrae.arachne.weave.SketchWeave;
import phanastrae.arachne.weave.element.Serializable;

public class SketchPhysicsMaterial extends SketchElement implements Serializable {
    public int id = -1;

    String name;

    public double density;
    public double elasticModulus;

    public SketchPhysicsMaterial() {
        this("Physics Materials");
    }

    public SketchPhysicsMaterial(String name) {
        this.name = name;
        this.density = 1;
        this.elasticModulus = 0;
    }

    public void setName(String name) {
        if(name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public void setElasticModulus(double elasticModulus) {
        this.elasticModulus = elasticModulus;
    }

    public double getDensity() {
        return this.density;
    }

    public double getElasticModulus() {
        return this.elasticModulus;
    }

    @Override
    public Text getTypeName() {
        return Text.of("Physics Material");
    }

    @Override
    public void read(NbtCompound nbt, SketchWeave sketchWeave) {
        this.name = nbt.getString("name");
        this.density = nbt.getDouble("density");
        this.elasticModulus = nbt.getDouble("em");
    }

    @Override
    public NbtCompound write() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putDouble("density", density);
        nbt.putDouble("em", elasticModulus);
        return nbt;
    }
}
