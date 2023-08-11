package phanastrae.arachne.weave.element.sketch;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.util.ArrayConversion;
import phanastrae.arachne.weave.SketchWeave;
import phanastrae.arachne.weave.element.Positionable;
import phanastrae.arachne.weave.element.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SketchTransform extends SketchElement implements Positionable, Serializable {
    public int id = -1;

    int parentLevel = 0;
    @Nullable
    public List<SketchElement> children;

    public double x;
    public double y;
    public double z;
    public double sizex = 1;
    public double sizey = 1;
    public double sizez = 1;
    public double pitch;
    public double yaw;
    public double roll;

    public SketchTransform() {
        this(null);
    }

    public SketchTransform(@Nullable SketchTransform parent) {
        this.parent = parent;
        updateParentLevel();
    }

    public int getParentLevel() {
        return this.parentLevel;
    }

    void updateParentLevel() {
        if(parent == null) {
            this.parentLevel = 0;
        } else {
            this.parentLevel = parent.parentLevel + 1;
        }
    }

    @Override
    public void setParent(@Nullable SketchTransform parent) {
        super.setParent(parent);
        updateParentLevel();
    }

    @Nullable
    public List<SketchElement> getChildren() {
        return this.children;
    }

    public void addChild(SketchElement child) {
        if(this.children == null) this.children = new ArrayList<>();;
        this.children.add(child);
    }

    public void removeChild(SketchElement child) {
        if(this.children == null) return;
        this.children.remove(child);
        if(this.children.isEmpty()) this.children = null;
    }

    public void forAllChildrenInTree(Consumer<SketchElement> action) {
        if(this.children == null) return;
        for(SketchElement child : this.children) {
            action.accept(child);
            if(child instanceof SketchTransform t) {
                t.forAllChildrenInTree(action);
            }
        }
    }

    public void setPosition(Vec3d pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public Vec3d getPosition() {
        return new Vec3d(this.x, this.y, this.z);
    }

    @Override
    public Text getTypeName() {
        return Text.translatable("arachne.editor.type.transform");
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public void setX(double d) {
        this.x = d;
    }

    @Override
    public void setY(double d) {
        this.y = d;
    }

    @Override
    public void setZ(double d) {
        this.z = d;
    }

    public double getPitchDeg() {
        return this.pitch;
    }

    public double getYawDeg() {
        return this.yaw;
    }

    public double getRollDeg() {
        return this.roll;
    }

    public double getPitchRad() {
        return Math.toRadians(this.pitch);
    }

    public double getYawRad() {
        return Math.toRadians(this.yaw);
    }

    public double getRollRad() {
        return Math.toRadians(this.roll);
    }

    public void setPitchDeg(double d) {
        d = d % 360;
        if(d <= -180) d += 360;
        if(d > 180) d -= 360;
        this.pitch = d;
    }

    public void setYawDeg(double d) {
        d = d % 360;
        if(d <= -180) d += 360;
        if(d > 180) d -= 360;
        this.yaw = d;
    }

    public void setRollDeg(double d) {
        d = d % 360;
        if(d <= -180) d += 360;
        if(d > 180) d -= 360;
        this.roll = d;
    }

    public void setPitchRad(double d) {
        this.setPitchDeg(Math.toDegrees(d));
    }

    public void setYawRad(double d) {
        this.setYawDeg(Math.toDegrees(d));
    }

    public void setRollRad(double d) {
        this.setRollDeg(Math.toDegrees(d));
    }

    public double getSizeX() {
        return this.sizex;
    }

    public double getSizeY() {
        return this.sizey;
    }

    public double getSizeZ() {
        return this.sizez;
    }

    public void setSizeX(double d) {
        this.sizex = d;
    }

    public void setSizeY(double d) {
        this.sizey = d;
    }

    public void setSizeZ(double d) {
        this.sizez = d;
    }

    public Matrix4f getTransform() {
        SketchTransform t = this;
        Matrix4f totalMat = new Matrix4f();
        for(int i = 0; i <= this.getParentLevel(); i++) {
            if(t == null) break;
            Matrix4f mat = new Matrix4f();
            mat.translate((float)t.x, (float)t.y, (float)t.z);
            mat.rotate(new Quaternionf().rotateYXZ((float)t.getYawRad(), (float)t.getPitchRad(), (float)t.getRollRad()));
            mat.scale((float)t.sizex, (float)t.sizey, (float)t.sizez);
            totalMat = mat.mul(totalMat);
            t = t.getParent();
        }
        return totalMat;
    }

    public Vec3d getLocalCoords(Vec3d globalCoords) {
        Matrix4f mat = this.getTransform().invert();
        Vector4f v = new Vector4f((float)globalCoords.x, (float)globalCoords.y, (float)globalCoords.z, 1);
        v = v.mul(mat);
        return new Vec3d(v.x, v.y, v.z);
    }

    @Override
    public void read(NbtCompound nbt, SketchWeave sketchWeave) {
        double[] d = ArrayConversion.longToDouble(nbt.getLongArray("d"));
        int p = nbt.getInt("p");
        boolean b = nbt.getBoolean("cd");
        if(d.length == 9) {
            x = d[0];
            y = d[1];
            z = d[2];
            sizex = d[3];
            sizey = d[4];
            sizez = d[5];
            pitch = d[6];
            yaw = d[7];
            roll = d[8];
        }
        SketchElement obj = sketchWeave.getObject(p);
        this.parent = obj instanceof SketchTransform ? (SketchTransform)obj : null;
        this.canDelete = b;
    }

    @Override
    public NbtCompound write() {
        NbtCompound nbt = new NbtCompound();
        nbt.putLongArray("d", ArrayConversion.doubleToLong(new double[]{x, y, z, sizex, sizey, sizez, pitch, yaw, roll}));
        int p = parent == null ? -1 : parent.id;
        nbt.putInt("p", p);
        nbt.putBoolean("cd", canDelete);
        return nbt;
    }
}
