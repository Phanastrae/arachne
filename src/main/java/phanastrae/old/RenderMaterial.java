package phanastrae.old;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

public class RenderMaterial {
    public int id = -1;

    public String name;
    @Nullable
    public Identifier texture;
    public float r = 1;
    public float g = 1;
    public float b = 1;
    public float a = 1;

    public RenderMaterial(String name) {
        this.name = name;
    }

    public RenderMaterial(String name, Identifier texture) {
        this(name);
        this.texture = texture;
    }

    public RenderMaterial(NbtCompound nbt) {
        this.loadFromNbtCompound(nbt);
    }

    public NbtCompound getNbtCompound() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", this.name);
        nbt.putString("namespace", this.texture == null ? "" : this.texture.getNamespace());
        nbt.putString("path", this.texture == null ? "" : this.texture.getPath());
        if(r != 1) {
            nbt.putFloat("r", this.r);
        }
        if(g != 1) {
            nbt.putFloat("g", this.g);
        }
        if(b != 1) {
            nbt.putFloat("b", this.b);
        }
        if(a != 1) {
            nbt.putFloat("a", this.a);
        }
        return nbt;
    }

    public void loadFromNbtCompound(NbtCompound nbt) {
        this.name = nbt.getString("name");
        this.texture = new Identifier(nbt.getString("namespace"), nbt.getString("path"));
        if(nbt.contains("r", NbtElement.FLOAT_TYPE)) {
            this.r = Math.clamp(0, 1, nbt.getFloat("r"));
        }
        if(nbt.contains("g", NbtElement.FLOAT_TYPE)) {
            this.g = Math.clamp(0, 1, nbt.getFloat("g"));
        }
        if(nbt.contains("b", NbtElement.FLOAT_TYPE)) {
            this.b = Math.clamp(0, 1, nbt.getFloat("b"));
        }
        if(nbt.contains("a", NbtElement.FLOAT_TYPE)) {
            this.a = Math.clamp(0, 1, nbt.getFloat("a"));
        }
    }

    public void setNamespace(String namespace) {
        if(this.texture == null) {
            this.setTexture(namespace, "");
        } else {
            this.setTexture(namespace, this.texture.getPath());
        }
    }

    public void setPath(String path) {
        if(this.texture == null) {
            this.setTexture("", path);
        } else {
            this.setTexture(this.texture.getNamespace(), path);
        }
    }

    public void setTexture(String namespace, String path) {
        if(namespace == null) namespace = "";
        if(path == null) path = "";

        if(namespace.equals("") && path.equals("")) {
            this.texture = null;
        } else {
            try {
                if (namespace.equals("")) {
                    this.texture = new Identifier(path);
                } else {
                    this.texture = new Identifier(namespace, path);
                }
            } catch (InvalidIdentifierException e) {
                this.texture = null;
            }
        }
    }
}
