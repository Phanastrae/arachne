package phanastrae.arachne.weave.element.built;

import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import phanastrae.arachne.weave.element.sketch.SketchFace;
import phanastrae.arachne.weave.element.sketch.SketchRenderMaterial;

import java.util.ArrayList;

public class BuiltRenderLayer {

    public Object vertexBufferHolder = null;

    int vertexCount;
    int indexCount;
    public ArrayList<BuiltFace> faces = new ArrayList<>();
    public ArrayList<Byte> rs = new ArrayList<>();
    public ArrayList<Byte> gs = new ArrayList<>();
    public ArrayList<Byte> bs = new ArrayList<>();
    public ArrayList<Byte> as = new ArrayList<>();
    public ArrayList<float[]> us = new ArrayList<>();
    public ArrayList<float[]> vs = new ArrayList<>();
    public ArrayList<Float> uAvg = new ArrayList<>();
    public ArrayList<Float> vAvg = new ArrayList<>();
    public ArrayList<float[]> usClean = new ArrayList<>();
    public ArrayList<float[]> vsClean = new ArrayList<>();
    public ArrayList<Float> uAvgClean = new ArrayList<>();
    public ArrayList<Float> vAvgClean = new ArrayList<>();

    long lastReload = Long.MIN_VALUE;

    int r;
    int g;
    int b;
    int a;

    String namespace;
    String path;
    boolean useTextureAtlas;

    public BuiltRenderLayer(SketchRenderMaterial renderMaterial) {
        this.r = renderMaterial.getR()&0xff;
        this.g = renderMaterial.getG()&0xff;
        this.b = renderMaterial.getB()&0xff;
        this.a = renderMaterial.getA()&0xff;
        this.namespace = renderMaterial.getNamespace();
        this.path = renderMaterial.getPath();
        this.useTextureAtlas = renderMaterial.getUseTextureAtlas();
        this.vertexCount = 0;
    }

    int calcVertexCount(BuiltFace face) {
        int c = 0;
        int nCount = face.nodes.length;
        int faceVertexCount = nCount + 1;
        if(face.doubleSided) {
            c += 2 * faceVertexCount;
        } else {
            c += faceVertexCount;
        }
        return c;
    }

    int calcIndexCount(BuiltFace face) {
        int c = 0;
        int nCount = face.nodes.length;
        int faceVertexCount = nCount * 3;
        if(face.doubleSided) {
            c += 2 * faceVertexCount;
        } else {
            c += faceVertexCount;
        }
        return c;
    }

    public void accept(SketchFace sf, BuiltFace bf, int layer) {
        this.faces.add(bf);
        this.rs.add((byte)((int)(r * (sf.r[layer]&0xff) / 255f) & 0xff));
        this.gs.add((byte)((int)(g * (sf.g[layer]&0xff) / 255f) & 0xff));
        this.bs.add((byte)((int)(b * (sf.b[layer]&0xff) / 255f) & 0xff));
        this.as.add((byte)((int)(a * (sf.a[layer]&0xff) / 255f) & 0xff));
        this.us.add(sf.u[layer]);
        this.vs.add(sf.v[layer]);
        this.usClean.add(sf.u[layer]);
        this.vsClean.add(sf.v[layer]);
        this.uAvg.add(avg(sf.u[layer]));
        this.vAvg.add(avg(sf.v[layer]));
        this.uAvgClean.add(avg(sf.u[layer]));
        this.vAvgClean.add(avg(sf.v[layer]));
        this.vertexCount += this.calcVertexCount(bf);
        this.indexCount += this.calcIndexCount(bf);
    }

    public float avg(float[] floats) {
        if(floats == null || floats.length == 0) return 0;
        float sum = 0;
        for(float f : floats) {
            sum += f;
        }
        return sum / floats.length;
    }

    public boolean needsUpdate(long latestReload) {
        return latestReload != this.lastReload;
    }

    public void setUVs(float u1, float v1, float u2, float v2, long latestReload) {
        int l = this.faces.size();
        for(int i = 0; i < l; i++) {
            float[] u = this.us.get(i);
            float[] v = this.vs.get(i);
            int l2 = u.length;
            float[] uclean = new float[l2];
            float[] vclean = new float[l2];
            for(int j = 0; j < l2; j++) {
                uclean[j] = u1 + (u2 - u1) * u[j];
                vclean[j] = v1 + (v2 - v1) * v[j];
            }
            this.uAvgClean.set(i, u1 + (u2 - u1) * this.uAvg.get(i));
            this.vAvgClean.set(i, v1 + (v2 - v1) * this.vAvg.get(i));
            this.usClean.set(i, uclean);
            this.vsClean.set(i, vclean);
        }
        this.lastReload = latestReload;
    }

    public Identifier getIdentifier() {
        if(this.namespace == null || this.path == null) return null;

        Identifier id;
        try{
            id = new Identifier(this.namespace, this.path);
            return id;
        } catch(InvalidIdentifierException e) {
            return null;
        }
    }

    public boolean getUseTextureAtlas() {
        return this.useTextureAtlas;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public int getIndexCount() {
        return this.indexCount;
    }
}
