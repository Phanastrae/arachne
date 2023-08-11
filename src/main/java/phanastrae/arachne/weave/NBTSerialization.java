package phanastrae.arachne.weave;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.joml.Math;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.util.ArrayConversion;
import phanastrae.arachne.weave.element.Serializable;
import phanastrae.arachne.weave.element.sketch.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static phanastrae.arachne.weave.element.sketch.SketchFace.LAYER_COUNT;

public class NBTSerialization {

    public static NbtCompound writeSketch(SketchWeave sketchWeave) {
        NbtCompound nbt = new NbtCompound();
        nbt.put("sketchData", writeSketchWeave(sketchWeave));
        return nbt;
    }

    public static NbtCompound writeWeave(SketchWeave sketchWeave) {
        NbtCompound nbt = new NbtCompound();
        nbt.put("weaveData", writeSketchWeave(sketchWeave));
        return nbt;
    }

    public static SketchWeave readSketch(NbtCompound nbt) {
        NbtCompound nbt2 = nbt.getCompound("sketchData");
        if(nbt2 == null || nbt.isEmpty()) return new SketchWeave();
        return readSketchWeave(nbt2);
    }

    public static SketchWeave readWeave(NbtCompound nbt) {
        NbtCompound nbt2 = nbt.getCompound("weaveData");
        if(nbt2 == null || nbt.isEmpty()) return new SketchWeave();
        return readSketchWeave(nbt2);
    }

    public static NbtCompound writeSketchWeave(SketchWeave sketchWeave) {
        try {
            sketchWeave.setIds();

            NbtCompound nbt = new NbtCompound();
            nbt.putInt("version", 1);
            nbt.put("settings", sketchWeave.settings.write());
            nbt.put("physicsMaterials", write(sketchWeave.physicsMaterials));
            nbt.put("renderMaterials", write(sketchWeave.renderMaterials));
            nbt.put("objects", writeObjects(sketchWeave));
            nbt.put("vertices", writeVertices(sketchWeave));
            nbt.put("edges", writeEdges(sketchWeave));
            nbt.put("faces", writeFaces(sketchWeave));
            return nbt;
        } catch(Exception e) {
            Arachne.LOGGER.error("Error converting sketch to nbt!");
            Arachne.LOGGER.warn(e.getMessage());
            Arachne.LOGGER.warn("suppressed: ");
            Arrays.stream(e.getSuppressed()).forEach((s) -> Arachne.LOGGER.warn(s.toString()));
            Arachne.LOGGER.warn("stack trace:");
            Arrays.stream(e.getStackTrace()).forEach((s) -> Arachne.LOGGER.warn(s.toString()));
            return null;
        }
    }

    public static NbtCompound writeObjects(SketchWeave sketchWeave) {
        NbtCompound nbt = new NbtCompound();
        nbt.put("transforms", write(sketchWeave.transforms));
        nbt.put("vertexCollections", write(sketchWeave.vertexCollections));
        return nbt;
    }

    public static NbtList write(List<? extends Serializable> list) {
        NbtList nbt = new NbtList();
        for(Serializable serializable : list) {
            nbt.add(serializable.write());
        }
        return nbt;
    }

    public static NbtCompound writeVertices(SketchWeave sketchWeave) {
        NbtCompound nbt = new NbtCompound();
        // run through in specific order
        List<SketchVertex> verticesOrdered = new ArrayList<>();
        for(SketchVertexCollection vc : sketchWeave.getVertexCollections()) {
            for(SketchVertex v : vc.getVertices()) {
                verticesOrdered.add(v);
            }
        }
        for(SketchVertex v : sketchWeave.getNodes()) {
            if(v.parent == null) {
                verticesOrdered.add(v);
            }
        }

        int l = verticesOrdered.size();
        double[] x = new double[l];
        double[] y = new double[l];
        double[] z = new double[l];
        boolean[] isStatic = new boolean[l];
        int[] phyMat = new int[l];
        double[] volume = new double[l];
        for(int i = 0; i < l; i++) {
            SketchVertex v = verticesOrdered.get(i);
            x[i] = v.x;
            y[i] = v.y;
            z[i] = v.z;
            isStatic[i] = v.isStatic;
            SketchPhysicsMaterial p = v.getPhysicsMaterial();
            phyMat[i] = p == null ? -1 : p.id;
            volume[i] = v.virtualVolume;
        }
        nbt.putLongArray("x", ArrayConversion.doubleToLong(x));
        nbt.putLongArray("y", ArrayConversion.doubleToLong(y));
        nbt.putLongArray("z", ArrayConversion.doubleToLong(z));
        nbt.putByteArray("isStatic", ArrayConversion.boolToByte(isStatic));
        nbt.putIntArray("phyMat", phyMat);
        nbt.putLongArray("volume", ArrayConversion.doubleToLong(volume));

        return nbt;
    }

    public static NbtCompound writeEdges(SketchWeave weave) {
        NbtCompound nbt = new NbtCompound();
        int l = weave.edges.size();
        int[] start = new int[l];
        int[] end = new int[l];
        double[] length = new double[l];
        int[] phyMat = new int[l];
        double[] radius = new double[l];
        boolean[] pullOnly = new boolean[l];
        for(int i = 0; i < l; i++) {
            SketchEdge e = weave.edges.get(i);
            start[i] = e.start.id;
            end[i] = e.end.id;
            length[i] = e.length;
            SketchPhysicsMaterial p = e.getPhysicsMaterial();
            phyMat[i] = p == null ? -1 : p.id;
            radius[i] = e.virtualRadius;
            pullOnly[i] = e.pullOnly;
        }
        nbt.putIntArray("start", start);
        nbt.putIntArray("end", end);
        nbt.putLongArray("length", ArrayConversion.doubleToLong(length));
        nbt.putIntArray("phyMat", phyMat);
        nbt.putLongArray("radius", ArrayConversion.doubleToLong(radius));
        nbt.putByteArray("pullOnly", ArrayConversion.boolToByte(pullOnly));
        return nbt;
    }

    public static NbtCompound writeFaces(SketchWeave weave) {
        NbtCompound nbt = new NbtCompound();
        int l = weave.faces.size();
        int j = 0;
        int[] vCount = new int[l];
        boolean[] doubleSided = new boolean[l];
        int[] phyMat = new int[l];
        int[] renMat = new int[l * LAYER_COUNT];
        double[] area = new double[l];
        byte[] r = new byte[l * LAYER_COUNT];
        byte[] g = new byte[l * LAYER_COUNT];
        byte[] b = new byte[l * LAYER_COUNT];
        byte[] a = new byte[l * LAYER_COUNT];
        for(int i = 0; i < l; i++) {
            SketchFace f = weave.faces.get(i);
            vCount[i] = f.vertices.length;
            area[i] = f.area;
            doubleSided[i] = f.doubleSided;
            SketchPhysicsMaterial p = f.getPhysicsMaterial();
            phyMat[i] = p == null ? -1 : p.id;
            j += vCount[i];
            for(int k = 0; k < LAYER_COUNT; k++) {
                renMat[k + i * LAYER_COUNT] = f.renderMaterial[k] == null ? -1 : f.renderMaterial[k].id;
                r[k + i * LAYER_COUNT] = (byte)f.r[k];
                g[k + i * LAYER_COUNT] = (byte)f.g[k];
                b[k + i * LAYER_COUNT] = (byte)f.b[k];
                a[k + i * LAYER_COUNT] = (byte)f.a[k];
            }
        }
        nbt.putIntArray("vCount", vCount);
        nbt.putByteArray("doubleSided", ArrayConversion.boolToByte(doubleSided));
        nbt.putIntArray("phyMat", phyMat);
        nbt.putIntArray("renMat", renMat);
        nbt.putByteArray("r", r);
        nbt.putByteArray("g", g);
        nbt.putByteArray("b", b);
        nbt.putByteArray("a", a);
        nbt.putLongArray("area", ArrayConversion.doubleToLong(area));

        int[] vertices = new int[j];
        float[] u = new float[j * LAYER_COUNT];
        float[] v = new float[j * LAYER_COUNT];
        int k = 0;
        for(SketchFace f : weave.faces) {
            for(int i = 0; i < f.vertices.length; i++) {
                int p = k + i;
                if(p < j) {
                    vertices[p] = f.vertices[i].id;
                    for(int layer = 0; layer < LAYER_COUNT; layer++) {
                        int index = layer + p * LAYER_COUNT;
                        if(index < j * LAYER_COUNT) {
                            u[index] = f.u[layer][i];
                            v[index] = f.v[layer][i];
                        }
                    }
                }
            }
            k += f.vertices.length;
        }
        nbt.putIntArray("vertices", vertices);
        nbt.putIntArray("u", ArrayConversion.floatToInt(u));
        nbt.putIntArray("v", ArrayConversion.floatToInt(v));
        return nbt;
    }

    public static SketchWeave readSketchWeave(NbtCompound nbt) {
        try {
            int version = nbt.getInt("version");
            if(version != 1) {
                nbt = upgrade(nbt);
            }

            SketchWeave sw = new SketchWeave();

            sw.settings.read(nbt.getCompound("settings"), sw);

            sw.physicsMaterials.addAll(makeList(nbt.getList("physicsMaterials", NbtElement.COMPOUND_TYPE), SketchPhysicsMaterial::new));
            readList(nbt.getList("physicsMaterials", NbtElement.COMPOUND_TYPE), sw.physicsMaterials, sw);

            sw.renderMaterials.addAll(makeList(nbt.getList("renderMaterials", NbtElement.COMPOUND_TYPE), SketchRenderMaterial::new));
            readList(nbt.getList("renderMaterials", NbtElement.COMPOUND_TYPE), sw.renderMaterials, sw);

            readObjects(nbt.getCompound("objects"), sw);

            readVertices(nbt.getCompound("vertices"), sw);
            readEdges(nbt.getCompound("edges"), sw);

            sw.markAllAsAdded();
            readFaces(nbt.getCompound("faces"), sw);
            for(SketchFace face : sw.faces) {
                face.add();
            }

            if(version != 1) {
                postUpgrade(version, sw);
            }
            return sw;
        } catch(Exception e) {
            Arachne.LOGGER.error("Error converting nbt to sketch!");
            Arachne.LOGGER.warn(e.getMessage());
            Arachne.LOGGER.warn("suppressed: ");
            Arrays.stream(e.getSuppressed()).forEach((s) -> Arachne.LOGGER.warn(s.toString()));
            Arachne.LOGGER.warn("stack trace:");
            Arrays.stream(e.getStackTrace()).forEach((s) -> Arachne.LOGGER.warn(s.toString()));
            return null;
        }
    }

    public static void readObjects(NbtCompound nbt, SketchWeave sketchWeave) {
        sketchWeave.transforms.clear();
        sketchWeave.transforms.addAll(makeList(nbt.getList("transforms", NbtElement.COMPOUND_TYPE), SketchTransform::new));
        sketchWeave.setRoot();
        sketchWeave.vertexCollections.addAll(makeList(nbt.getList("vertexCollections", NbtElement.COMPOUND_TYPE), SketchVertexCollection::new));
        sketchWeave.setIds();

        readList(nbt.getList("transforms", NbtElement.COMPOUND_TYPE), sketchWeave.transforms, sketchWeave);
        readList(nbt.getList("vertexCollections", NbtElement.COMPOUND_TYPE), sketchWeave.vertexCollections, sketchWeave);
    }

    public static <T extends Serializable> ArrayList<T> makeList(NbtList nbt, Supplier<T> sup) {
        ArrayList<T> list = new ArrayList<>();
        for(int i = 0; i < nbt.size(); i++) {
            list.add(sup.get());
        }
        return list;
    }

    public static <T extends Serializable> void readList(NbtList nbt, List<T> list, SketchWeave sketchWeave) {
        for(int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            t.read(nbt.getCompound(i), sketchWeave);
        }
    }

    public static void readVertices(NbtCompound nbt, SketchWeave sketchWeave) {
        List<SketchVertex> vertices = new ArrayList<>();
        double[] x = ArrayConversion.longToDouble(nbt.getLongArray("x"));
        double[] y = ArrayConversion.longToDouble(nbt.getLongArray("y"));
        double[] z = ArrayConversion.longToDouble(nbt.getLongArray("z"));
        boolean[] isStatic = ArrayConversion.byteToBool(nbt.getByteArray("isStatic"));
        int[] phyMat = nbt.getIntArray("phyMat");
        double[] volume = ArrayConversion.longToDouble(nbt.getLongArray("volume"));
        int l = Math.min(x.length, Math.min(y.length, z.length));
        for(int i = 0; i < l; i++) {
            SketchVertex v = new SketchVertex(null);
            v.x = x[i];
            v.y = y[i];
            v.z = z[i];
            if(i < isStatic.length) {
                v.isStatic = isStatic[i];
            }
            if(i < phyMat.length) {
                int p = phyMat[i];
                if(0 <= p && p < sketchWeave.getPhysicsMaterials().size()) {
                    v.setPhysicsMaterial(sketchWeave.getPhysicsMaterials().get(p));
                }
            }
            if(i < volume.length) {
                v.virtualVolume = volume[i];
            }
            vertices.add(v);
        }

        int j = 0;
        for(SketchVertexCollection vc : sketchWeave.getVertexCollections()) {
            for(int i = 0; i < vc.vertexCount; i++) {
                int k = j + i;
                if(0 <= k && k < vertices.size()) {
                    vertices.get(k).parent = vc;
                }
            }
            j += vc.vertexCount;
        }
        sketchWeave.nodes.addAll(vertices);
    }

    public static void readEdges(NbtCompound nbt, SketchWeave sketchWeave) {
        int[] start = nbt.getIntArray("start");
        int[] end = nbt.getIntArray("end");
        double[] length = ArrayConversion.longToDouble(nbt.getLongArray("length"));
        int[] phyMat = nbt.getIntArray("phyMat");
        double[] radius = ArrayConversion.longToDouble(nbt.getLongArray("radius"));
        boolean[] pullOnly = ArrayConversion.byteToBool(nbt.getByteArray("pullOnly"));
        int l = Math.min(start.length, end.length);
        for(int i = 0; i < l; i++) {
            SketchEdge e = new SketchEdge(sketchWeave.nodes.get(start[i]), sketchWeave.nodes.get(end[i]));
            if(i < length.length) {
                e.length = length[i];
            }
            if(i < phyMat.length) {
                int p = phyMat[i];
                if (0 <= p && p < sketchWeave.physicsMaterials.size()) {
                    e.setPhysicsMaterial(sketchWeave.physicsMaterials.get(p));
                }
            }
            if(i < radius.length) {
                e.virtualRadius = radius[i];
            }
            if(i < pullOnly.length) {
                e.pullOnly = pullOnly[i];
            }
            sketchWeave.edges.add(e);
        }
    }

    public static void readFaces(NbtCompound nbt, SketchWeave sketchWeave) {
        int[] vCount = nbt.getIntArray("vCount");
        boolean[] doubleSided = ArrayConversion.byteToBool(nbt.getByteArray("doubleSided"));
        int[] phyMat = nbt.getIntArray("phyMat");
        double[] area = ArrayConversion.longToDouble(nbt.getLongArray("area"));
        int[] renMat = nbt.getIntArray("renMat");
        byte[] r = nbt.getByteArray("r");
        byte[] g = nbt.getByteArray("g");
        byte[] b = nbt.getByteArray("b");
        byte[] a = nbt.getByteArray("a");
        int[] vertices = nbt.getIntArray("vertices");
        float[] u = ArrayConversion.intToFloat(nbt.getIntArray("u"));
        float[] v = ArrayConversion.intToFloat(nbt.getIntArray("v"));
        int idx = 0;
        int l = vCount.length;
        int k = 0;
        for(int i = 0; i < l; i++) {
            int vc = vCount[i];
            ArrayList<SketchVertex> vertex = new ArrayList<>();
            for(int j = 0; j < vc; j++) {
                int id = vertices[k + j];
                if(0 <= id && id < sketchWeave.nodes.size()) {
                    vertex.add(sketchWeave.nodes.get(id));
                }
            }
            k += vc;

            SketchFace f = new SketchFace(vertex);
            if(i < doubleSided.length) {
                f.doubleSided = doubleSided[i];
            }
            if(i < phyMat.length) {
                int p = phyMat[i];
                if(0 <= p && p < sketchWeave.getPhysicsMaterials().size()) {
                    f.setPhysicsMaterial(sketchWeave.getPhysicsMaterials().get(p));
                }
            }
            if(i < area.length) {
                f.area = area[i];
            }
            for(int j = 0; j < LAYER_COUNT; j++) {
                int m = j + LAYER_COUNT * i;
                if(i < renMat.length) {
                    int ren = renMat[m];
                    if(0 <= ren && ren < sketchWeave.renderMaterials.size()) {
                        f.setRenderMaterial(sketchWeave.renderMaterials.get(ren), j);
                    }
                }
                if(j < r.length) {
                    f.r[j] = r[m]&0xff;
                }
                if(j < g.length) {
                    f.g[j] = g[m]&0xff;
                }
                if(j < b.length) {
                    f.b[j] = b[m]&0xff;
                }
                if(j < a.length) {
                    f.a[j] = a[m]&0xff;
                }
            }
            for(int n = 0; n < f.vertices.length; n++) {
                for(int j = 0; j < LAYER_COUNT; j++) {
                    if(idx < u.length && idx < v.length) {
                        f.u[j][n] = Math.clamp(0, 1, u[idx]);
                        f.v[j][n] = Math.clamp(0, 1, v[idx]);
                        idx++;
                    }
                }
            }
            sketchWeave.addElement(f);
        }
    }

    public static NbtCompound upgrade(NbtCompound nbt) {
        NbtCompound nbt2 = nbt.copy();
        int version = nbt2.getInt("version");
        if(version == 0 && !nbt2.contains("vertices")) { // update from 0.0.1.0's format
            rename(nbt2, "nodes", "vertices");
            rename(nbt2, "links", "edges");

            NbtCompound vertices = nbt2.getCompound("vertices");
            NbtCompound edges = nbt2.getCompound("edges");
            NbtCompound faces = nbt2.getCompound("faces");

            double[] mass = ArrayConversion.longToDouble(vertices.getLongArray("mass"));
            int[] pmVertices = new int[mass.length];
            for(int i = 0; i < mass.length; i++) {
                pmVertices[i] = mass[i] == 0 ? -1 : 0;
            }

            vertices.putIntArray("phyMat", pmVertices);

            rename(vertices, "mass", "volume");
            rename(edges, "link1", "start");
            rename(edges, "link2", "end");
            rename(edges, "idealLength", "length");
            rename(faces, "faceSize", "vCount");
            rename(faces, "faceNodeIds", "vertices");

            double[] stiffness = ArrayConversion.longToDouble(edges.getLongArray("stiffness"));
            double[] length = ArrayConversion.longToDouble(edges.getLongArray("length"));
            double[] radius = new double[stiffness.length];
            int[] phyMat = new int[stiffness.length];
            for(int i = 0; i < stiffness.length; i++) {
                double len = i < length.length ? length[i] : 0;
                double elasticModulus = 1E7;
                radius[i] = Math.sqrt(stiffness[i] * len / Math.PI / elasticModulus);

                if(stiffness[i] != 0) {
                    phyMat[i] = 1;
                } else {
                    phyMat[i] = -1;
                }
            }
            edges.remove("stiffness");
            edges.putLongArray("radius", ArrayConversion.doubleToLong(radius));
            edges.putIntArray("phyMat", phyMat);

            byte[] r = faces.getByteArray("r");
            byte[] g = faces.getByteArray("g");
            byte[] b = faces.getByteArray("b");
            int[] rm = faces.getIntArray("renderMaterial");
            float[] u = ArrayConversion.intToFloat(faces.getIntArray("faceNodeUs"));
            float[] v = ArrayConversion.intToFloat(faces.getIntArray("faceNodeVs"));

            byte[] r2 = new byte[r.length * LAYER_COUNT];
            byte[] g2 = new byte[g.length * LAYER_COUNT];
            byte[] b2 = new byte[b.length * LAYER_COUNT];
            byte[] a2 = new byte[r.length * LAYER_COUNT];
            for(int i = 0; i < r2.length; i++) {
                r2[i] = (byte)255;
                if(i < g2.length) g2[i] = (byte)255;
                if(i < b2.length) b2[i] = (byte)255;
                a2[i] = (byte)255;
            }
            int[] rm2 = new int[rm.length * LAYER_COUNT];
            Arrays.fill(rm2, -1);
            float[] u2 = new float[u.length * LAYER_COUNT];
            float[] v2 = new float[v.length * LAYER_COUNT];
            for(int i = 0; i < r.length; i++) {
                r2[i * LAYER_COUNT] = r[i];
            }
            for(int i = 0; i < g.length; i++) {
                g2[i * LAYER_COUNT] = g[i];
            }
            for(int i = 0; i < b.length; i++) {
                b2[i * LAYER_COUNT] = b[i];
            }
            for(int i = 0; i < rm.length; i++) {
                rm2[i * LAYER_COUNT] = rm[i];
            }

            int k = 0;
            int m = 0;
            int[] faceSizes = faces.getIntArray("vCount");
            for(int i = 0; i < faceSizes.length; i++) {
                int size = faceSizes[i];
                for(int j = 0; j < size; j++) {
                    if(m >= u.length || m >= v.length || k >= u2.length || k >= v2.length) break;
                    u2[k] = u[m];
                    v2[k] = v[m];
                    m++;
                    k+= 4;
                }
            }

            boolean[] doubleSided = new boolean[faceSizes.length];
            Arrays.fill(doubleSided, true);

            faces.remove("r");
            faces.remove("g");
            faces.remove("b");
            faces.remove("renderMaterial");
            faces.remove("faceNodeUs");
            faces.remove("faceNodeVs");
            faces.putByteArray("r", r2);
            faces.putByteArray("g", g2);
            faces.putByteArray("b", b2);
            faces.putByteArray("a", a2);
            faces.putIntArray("renMat", rm2);
            faces.putIntArray("u", ArrayConversion.floatToInt(u2));
            faces.putIntArray("v", ArrayConversion.floatToInt(v2));
            faces.putByteArray("doubleSided", ArrayConversion.boolToByte(doubleSided));

            SketchTransform root = new SketchTransform();
            root.id = 0;
            root.setDeletable(false);
            SketchVertexCollection svc = new SketchVertexCollection(root);
            svc.id = 1;
            for(int i = 0; i < nbt2.getCompound("vertices").getLongArray("x").length; i++) {
                svc.addChild(new SketchVertex(svc));
            }

            NbtCompound objects = new NbtCompound();
            objects.put("transforms", write(List.of(root)));
            objects.put("vertexCollections", write(List.of(svc)));

            SketchPhysicsMaterial pm = new SketchPhysicsMaterial();
            pm.setName("MATERIAL_NODE");
            pm.density = 1;
            pm.elasticModulus = 0;
            SketchPhysicsMaterial pm2 = new SketchPhysicsMaterial();
            pm2.setName("MATERIAL_LINK");
            pm2.density = 0;
            pm2.elasticModulus = 1E7;
            nbt2.put("physicsMaterials", write(List.of(pm, pm2)));

            nbt2.put("objects", objects);

            NbtCompound renderMaterials = nbt2.getCompound("renderMaterials");
            nbt2.remove("renderMaterials");
            nbt2.put("renderMaterials", renderMaterials.getList("values", NbtElement.COMPOUND_TYPE));

            NbtList renMats = nbt2.getList("renderMaterials", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < renMats.size(); i++) {
                NbtCompound nc = renMats.getCompound(i);
                nc.putBoolean("useTexAtlas", true);
                nc.remove("r");
                nc.remove("g");
                nc.remove("b");
                nc.remove("a");
                // r,g,b,a were not used for render materials previously
                nc.putInt("color", 0xffffffff);
            }
            SketchSettings settings = new SketchSettings();
            settings.setStepWind(3);
            nbt2.put("settings", settings.write());
        }
        return nbt2;
    }

    public static void rename(NbtCompound nbt, String oldName, String newName) {
        NbtElement e = nbt.get(oldName);
        nbt.remove(oldName);
        nbt.put(newName, e);
    }

    public static void postUpgrade(int version, SketchWeave sw) {
        if(version == 0) {
            for(SketchFace face : sw.faces) {
                face.setArea(-1);
            }
        }
    }
}
