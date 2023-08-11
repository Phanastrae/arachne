package phanastrae.arachne.weave.element.sketch;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.weave.SketchWeave;

import java.util.ArrayList;
import java.util.List;

public class SketchVertexCollection extends SketchTransform {

    // vertex count only used for serialisation
    public int vertexCount = 0;

    public SketchVertexCollection() {
        this(null);
    }

    public SketchVertexCollection(@Nullable SketchTransform parent) {
        super(parent);
    }

    @Override
    public Text getTypeName() {
        return Text.translatable("arachne.editor.type.vertexCollection");
    }

    public List<SketchVertex> getVertices() {
        if(this.children == null) return List.of();
        List<SketchVertex> list = new ArrayList<>();
        for(SketchElement e : this.children) {
            if(e instanceof SketchVertex v) {
                list.add(v);
            }
        }
        return list;
    }

    @Override
    public void read(NbtCompound nbt, SketchWeave sketchWeave) {
        super.read(nbt, sketchWeave);
        this.vertexCount = nbt.getInt("vcount");
    }

    @Override
    public NbtCompound write() {
        NbtCompound nbt = super.write();
        List<SketchVertex> v = this.getVertices();
        nbt.putInt("vcount", v == null ? 0 : v.size());
        return nbt;
    }
}
