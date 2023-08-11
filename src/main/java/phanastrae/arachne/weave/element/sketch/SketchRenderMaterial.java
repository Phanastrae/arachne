package phanastrae.arachne.weave.element.sketch;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.weave.SketchWeave;
import phanastrae.arachne.weave.element.Serializable;

public class SketchRenderMaterial extends SketchElement implements Serializable {
    public int id = -1;

    String name;
    byte r = (byte)0xff;
    byte g = (byte)0xff;
    byte b = (byte)0xff;
    byte a = (byte)0xff;
    String namespace;
    String path;
    boolean useTextureAtlas = true;

    public SketchRenderMaterial() {
        this("Render Material");
    }

    public SketchRenderMaterial(String name) {
        this.name = name;
    }

    public void setName(String name) {
        if(name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getPath() {
        return this.path;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setR(int r) {
        this.r = (byte)clampColor(r);
    }

    public void setG(int g) {
        this.g = (byte)clampColor(g);
    }

    public void setB(int b) {
        this.b = (byte)clampColor(b);
    }

    public void setA(int a) {
        this.a = (byte)clampColor(a);
    }

    public int clampColor(int c) {
        if(c < 0) return 0;
        if(c > 255) return 255;
        return c;
    }

    public int getR() {
        return ((int)this.r) & 0xff;
    }

    public int getG() {
        return ((int)this.g) & 0xff;
    }

    public int getB() {
        return ((int)this.b) & 0xff;
    }

    public int getA() {
        return ((int)this.a) & 0xff;
    }

    public int getColor() {
        int r = this.getR();
        int g = this.getG();
        int b = this.getB();
        int a = this.getA();
        return (r << 24) | (g << 16) | (b << 8) | (a);
    }

    public void setColor(int col) {
        int r = col & 0xff000000;
        int g = col & 0x00ff0000;
        int b = col & 0x0000ff00;
        int a = col & 0x000000ff;
        this.setR((r >> 24) & 0xff);
        this.setG((g >> 16) & 0xff);
        this.setB((b >> 8) & 0xff);
        this.setA(a & 0xff);
    }

    public boolean getUseTextureAtlas() {
        return this.useTextureAtlas;
    }

    public void setUseTextureAtlas(boolean b) {
        this.useTextureAtlas = b;
    }

    @Override
    public Text getTypeName() {
        return Text.of("Render Material");
    }

    @Override
    public void read(NbtCompound nbt, SketchWeave sketchWeave) {
        this.name = nbt.getString("name");
        this.setColor(nbt.getInt("color"));
        this.namespace = nbt.getString("namespace");
        this.path = nbt.getString("path");
        this.useTextureAtlas = nbt.getBoolean("useTexAtlas");
    }

    @Override
    public NbtCompound write() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putInt("color", getColor());
        nbt.putString("namespace", namespace);
        nbt.putString("path", path);
        nbt.putBoolean("useTexAtlas", this.useTextureAtlas);
        return nbt;
    }
}
