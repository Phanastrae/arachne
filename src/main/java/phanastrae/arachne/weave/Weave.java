package phanastrae.arachne.weave;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import phanastrae.arachne.weave.link_type.Link;
import phanastrae.arachne.weave.link_type.StringLink;
import phanastrae.arachne.util.ArrayConversion;

import java.util.ArrayList;
import java.util.HashMap;

public class Weave {

    public Weave() {
        // TODO: add these in game
        //this.addRenderMaterial("white_wool", new Identifier("block/white_wool"));
        //this.addRenderMaterial("oak_planks", new Identifier("block/oak_planks"));
    }

    public Weave(NbtCompound nbt) {
        this();
        this.readFromNBT(nbt);
    }

    public ArrayList<Node> nodes = new ArrayList<>();
    public ArrayList<Link> links = new ArrayList<>();
    public ArrayList<Face> faces = new ArrayList<>();
    public HashMap<String, RenderMaterial> renderMaterials = new HashMap<>();

    public boolean addNode(Node node) {
        if(node == null) return false;
        this.nodes.add(node);
        return true;
    }

    public boolean addLink(Link link) {
        if(link == null) return false;
        this.links.add(link);
        return true;
    }

    public boolean addFace(Face face) {
        if(face == null) return false;
        this.faces.add(face);
        return true;
    }

    public boolean addRenderMaterial(String name, Identifier id) {
        if(name == null || id == null) return false;
        if(this.renderMaterials.containsKey(name)) return false;
        this.renderMaterials.put(name, new RenderMaterial(name, id));
        return true;
    }

    public void assignIds() {
        int nextId = 0;
        for(Node n : this.nodes) {
            n.id = nextId;
            nextId++;
        }
        nextId = 0;
        for(RenderMaterial renderMaterial : this.renderMaterials.values()) {
            renderMaterial.id = nextId;
            nextId++;
        }
    }

    public void writeToNBT(NbtCompound nbt) {
        this.assignIds(); // assign ids to elements based on position in arrays
        NbtCompound nodes = new NbtCompound();
        NbtCompound links = new NbtCompound();
        NbtCompound faces = new NbtCompound();
        NbtCompound renderMaterials = new NbtCompound();
        writeNodes(nodes);
        writeLinks(links);
        writeFaces(faces);
        writeRenderMaterials(renderMaterials);
        if(!nodes.isEmpty()) {
            nbt.put("nodes", nodes);
        }
        if(!links.isEmpty()) {
            nbt.put("links", links);
        }
        if(!faces.isEmpty()) {
            nbt.put("faces", faces);
        }
        if(!renderMaterials.isEmpty()) {
            nbt.put("renderMaterials", renderMaterials);
        }
    }

    public void writeNodes(NbtCompound nbt) {
        int nodeCount = this.nodes.size();
        if(nodeCount == 0) {
            return;
        }
        double[] x = new double[nodeCount];
        double[] y = new double[nodeCount];
        double[] z = new double[nodeCount];
        double[] mass = new double[nodeCount];
        boolean[] isStatic = new boolean[nodeCount];
        for(int i = 0; i < nodeCount; i++) {
            Node n = this.nodes.get(i);
            x[i] = n.pos.x;
            y[i] = n.pos.y;
            z[i] = n.pos.z;
            mass[i] = n.mass;
            isStatic[i] = n.isStatic;
        }
        nbt.putLongArray("x", ArrayConversion.doubleToLong(x));
        nbt.putLongArray("y", ArrayConversion.doubleToLong(y));
        nbt.putLongArray("z", ArrayConversion.doubleToLong(z));
        nbt.putLongArray("mass", ArrayConversion.doubleToLong(mass));
        nbt.putByteArray("isStatic", ArrayConversion.boolToByte(isStatic));
    }

    public void writeLinks(NbtCompound nbt) {
        int linkCount = this.links.size(); // TODO: really should change how links/edges work
        if(linkCount == 0) {
            return;
        }
        int[] link1 = new int[linkCount];
        int[] link2 = new int[linkCount];
        double[] idealLength = new double[linkCount];
        double[] stiffness = new double[linkCount];
        boolean[] pullOnly = new boolean[linkCount];
        for(int i = 0; i < linkCount; i++) {
            Link l = this.links.get(i);
            link1[i] = l.node1.id;
            link2[i] = l.node2.id;
            if (l instanceof StringLink sl) {
                idealLength[i] = sl.idealLength;
                stiffness[i] = sl.strengthConstant;
                pullOnly[i] = sl.pullOnly;
            }
        }
        nbt.putIntArray("link1", link1);
        nbt.putIntArray("link2", link2);
        nbt.putLongArray("idealLength", ArrayConversion.doubleToLong(idealLength));
        nbt.putLongArray("stiffness", ArrayConversion.doubleToLong(stiffness));
        nbt.putByteArray("pullOnly", ArrayConversion.boolToByte(pullOnly));
    }

    public void writeFaces(NbtCompound nbt) {
        int faceCount = this.faces.size();
        if(faceCount == 0) {
            return;
        }
        byte[] r = new byte[faceCount];
        byte[] g = new byte[faceCount];
        byte[] b = new byte[faceCount];
        int[] rm = new int[faceCount];
        int[] faceSize = new int[faceCount];
        int totalSize = 0;
        for(int i = 0; i < faceCount; i++) {
            Face face = this.faces.get(i);
            r[i] = (byte)face.r;
            g[i] = (byte)face.g;
            b[i] = (byte)face.b;
            rm[i] = face.renderMaterial == null ? -1 : face.renderMaterial.id;
            int size = face.nodes.length;
            faceSize[i] = size;
            totalSize += size;
        }
        int nextFaceNode = 0;
        int[] faceNodeIds = new int[totalSize];
        float[] ul = new float[totalSize];
        float[] vl = new float[totalSize];
        for(int i = 0; i < faceCount; i++) {
            Face face = this.faces.get(i);
            for(int j = 0; j < face.nodes.length; j++) {
                int id = face.nodes[j].id;
                faceNodeIds[nextFaceNode] = id;
                ul[nextFaceNode] = face.ul[j];
                vl[nextFaceNode] = face.vl[j];
                nextFaceNode++;
            }
        }
        nbt.putByteArray("r", r);
        nbt.putByteArray("g", g);
        nbt.putByteArray("b", b);
        nbt.putIntArray("renderMaterial", rm);
        nbt.putIntArray("faceSize", faceSize);
        nbt.putIntArray("faceNodeIds", faceNodeIds);
        nbt.putIntArray("faceNodeUs", ArrayConversion.floatToInt(ul));
        nbt.putIntArray("faceNodeVs", ArrayConversion.floatToInt(vl));
    }

    public void writeRenderMaterials(NbtCompound nbt) {
        int count = this.renderMaterials.size();
        if(count == 0) {
            return;
        }
        NbtList nbtList = new NbtList();
        for(RenderMaterial renderMaterial : this.renderMaterials.values()) {
            nbtList.add(renderMaterial.getNbtCompound());
        }
        nbt.put("values", nbtList);
    }

    public void readFromNBT(NbtCompound nbt) {
        readNodes(nbt.getCompound("nodes"));
        readLinks(nbt.getCompound("links"));
        RenderMaterial[] rm = readRenderMaterials(nbt.getCompound("renderMaterials"));
        readFaces(nbt.getCompound("faces"), rm);
    }

    public void readNodes(NbtCompound nbt) {
        this.nodes.clear();
        double[] x = ArrayConversion.longToDouble(nbt.getLongArray("x"));
        double[] y = ArrayConversion.longToDouble(nbt.getLongArray("y"));
        double[] z = ArrayConversion.longToDouble(nbt.getLongArray("z"));
        double[] mass = ArrayConversion.longToDouble(nbt.getLongArray("mass"));
        boolean[] isStatic = ArrayConversion.byteToBool(nbt.getByteArray("isStatic"));
        if(x.length != y.length || y.length != z.length) return; // invalid
        int length = x.length;
        for(int i = 0; i < length; i++) {
            Node node = new Node(new Vec3d(x[i], y[i], z[i]));
            if(i < mass.length) {
                node.mass = mass[i];
            }
            if(i < isStatic.length) {
                node.isStatic = isStatic[i];
            }
            this.addNode(node);
        }
    }

    public void readLinks(NbtCompound nbt) {
        this.links.clear();
        int[] link1 = nbt.getIntArray("link1");
        int[] link2 = nbt.getIntArray("link2");
        double[] idealLength = ArrayConversion.longToDouble(nbt.getLongArray("idealLength"));
        double[] stiffness = ArrayConversion.longToDouble(nbt.getLongArray("stiffness"));
        boolean[] pullOnly = ArrayConversion.byteToBool(nbt.getByteArray("pullOnly"));
        if(link1.length != link2.length) return; // invalid
        int length = link1.length;
        for(int i = 0; i < length; i++) {
            long id1 = link1[i];
            long id2 = link2[i];
            if(id1 >= this.nodes.size() || id2 >= this.nodes.size()) continue;
            StringLink link = new StringLink(this.nodes.get((int)id1), this.nodes.get((int)id2));
            if(i < idealLength.length) {
                link.idealLength = idealLength[i];
            }
            if(i < stiffness.length) {
                link.strengthConstant = stiffness[i];
            }
            if(i < pullOnly.length) {
                link.pullOnly = pullOnly[i];
            }
            this.addLink(link);
        }
    }

    public void readFaces(NbtCompound nbt, RenderMaterial[] renderMaterialsOrdered) {
        this.faces.clear();
        int[] faceSize = nbt.getIntArray("faceSize");
        int[] faceNodeIds = nbt.getIntArray("faceNodeIds");
        float[] ul = ArrayConversion.intToFloat(nbt.getIntArray("faceNodeUs"));
        float[] vl = ArrayConversion.intToFloat(nbt.getIntArray("faceNodeVs"));
        byte[] r = nbt.getByteArray("r");
        byte[] g = nbt.getByteArray("g");
        byte[] b = nbt.getByteArray("b");
        int[] rm = nbt.getIntArray("renderMaterial");
        int nextId = 0;
        boolean exit = false;
        for(int i = 0; i < faceSize.length; i++) {
            int size = faceSize[i];
            ArrayList<Node> fNodes = new ArrayList<>();
            float[] fUs = new float[size];
            float[] fVs = new float[size];
            for(int j = 0; j < size; j++) {
                if(nextId >= faceNodeIds.length) {
                    exit = true; // in case of malformed arrays
                    break;
                }
                int id = faceNodeIds[nextId];
                if(id >= nodes.size()) {
                    exit = true;
                    break;
                }
                fNodes.add(nodes.get(id));
                if(nextId < ul.length && nextId < vl.length) {
                    fUs[j] = ul[nextId];
                    fVs[j] = vl[nextId];
                }
                nextId++;
            }
            if(exit) {
                continue;
            }
            Face face = new Face(fNodes);
            if(i < r.length && i < g.length && i < b.length) {
                face.r = (int)r[i]&0xFF;
                face.g = (int)g[i]&0xFF;
                face.b = (int)b[i]&0xFF;
            }
            if(i < rm.length) {
                int rmID = rm[i];
                if(0 <= rmID && rmID < renderMaterialsOrdered.length) {
                    face.renderMaterial = renderMaterialsOrdered[rmID];
                }
            }
            if(ul.length > 0 && vl.length > 0) {
                face.ul = fUs;
                face.vl = fVs;
            }
            this.addFace(face);
        }
    }

    public RenderMaterial[] readRenderMaterials(NbtCompound nbt) {
        if(!nbt.contains("values", NbtElement.LIST_TYPE)) return new RenderMaterial[0];

        NbtList nbtList = nbt.getList("values", NbtElement.COMPOUND_TYPE);
        RenderMaterial[] renderMaterialReturn = new RenderMaterial[nbtList.size()];
        for(int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            RenderMaterial rm = new RenderMaterial(nbtCompound);
            if(!this.renderMaterials.containsKey(rm.name)) {
                this.renderMaterials.put(rm.name, rm);
                renderMaterialReturn[i] = rm;
            }
        }
        return renderMaterialReturn;
    }
}
