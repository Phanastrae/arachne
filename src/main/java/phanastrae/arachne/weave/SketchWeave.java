package phanastrae.arachne.weave;

import phanastrae.arachne.weave.element.built.*;
import phanastrae.arachne.weave.element.sketch.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SketchWeave {

    SketchTransform root = new SketchTransform(null);

    ArrayList<SketchVertex> nodes;
    ArrayList<SketchEdge> edges;
    ArrayList<SketchFace> faces;
    ArrayList<SketchTransform> transforms;
    ArrayList<SketchVertexCollection> vertexCollections;

    SketchSettings settings;

    ArrayList<SketchPhysicsMaterial> physicsMaterials;
    ArrayList<SketchRenderMaterial> renderMaterials;

    HashMap<Class<? extends SketchElement>, ArrayList<SketchElement>> elementMap = new HashMap<>();

    public SketchWeave() {
        this.nodes = getAndAddList(SketchVertex.class);
        this.edges = getAndAddList(SketchEdge.class);
        this.faces = getAndAddList(SketchFace.class);
        this.transforms = getAndAddList(SketchTransform.class);
        this.vertexCollections = getAndAddList(SketchVertexCollection.class);
        this.physicsMaterials = getAndAddList(SketchPhysicsMaterial.class);
        this.renderMaterials = getAndAddList(SketchRenderMaterial.class);
        this.settings = new SketchSettings();

        this.root.setDeletable(false);
        this.addElement(this.root);
    }

    public SketchTransform getRoot() {
        return this.root;
    }

    public <T extends SketchElement> ArrayList<T> getAndAddList(Class<T> klass) {
        ArrayList<T> list = new ArrayList<>();
        this.elementMap.put(klass, (ArrayList<SketchElement>) list);
        return list;
    }

    public void addElement(SketchElement element) {
        if(element.add()) {
            elementMap.get(element.getClass()).add(element);
        }
    }

    public void removeElement(SketchElement element) {
        if (element.remove()) {
            elementMap.get(element.getClass()).remove(element);
        }
    }

    public void addElements(List<? extends SketchElement> elements) {
        for(SketchElement element : elements) {
            this.addElement(element);
        }
    }

    public void removeElements(List<? extends SketchElement> elements) {
        for(SketchElement element : elements) {
            this.removeElement(element);
        }
    }

    public ArrayList<SketchVertex> getNodes() {
        return this.nodes;
    }

    public ArrayList<SketchEdge> getEdges() {
        return this.edges;
    }

    public ArrayList<SketchFace> getFaces() {
        return this.faces;
    }

    public ArrayList<SketchTransform> getRigidBodies() {
        return this.transforms;
    }

    public ArrayList<SketchVertexCollection> getVertexCollections() {
        return this.vertexCollections;
    }

    public ArrayList<SketchRenderMaterial> getRenderMaterials() {
        return this.renderMaterials;
    }

    public ArrayList<SketchPhysicsMaterial> getPhysicsMaterials() {
        return this.physicsMaterials;
    }

    public SketchSettings getSettings() {
        return this.settings;
    }

    public void clear() {
        this.nodes.clear();
        this.edges.clear();
        this.faces.clear();
        this.transforms.clear();
        this.vertexCollections.clear();
    }

    public BuiltWeave buildWeave() {
        this.setIds();

        BuiltNode[] nodes = new BuiltNode[this.nodes.size()];
        BuiltEdge[] edges = new BuiltEdge[this.edges.size()];
        BuiltFace[] faces = new BuiltFace[this.faces.size()];
        BuiltRigidBody[] transforms = new BuiltRigidBody[this.transforms.size()];

        for(int i = 0; i < this.nodes.size(); i++) {
            SketchVertex node = this.nodes.get(i);
            node.id = i;
            nodes[i] = new BuiltNode(node);
        }

        for(int i = 0; i < this.edges.size(); i++) {
            SketchEdge edge = this.edges.get(i);
            edge.id = i;
            edges[i] = new BuiltEdge(edge);

            double edgeMass = edge.getMass();
            nodes[edges[i].startNode].addMass(edgeMass / 2);
            nodes[edges[i].endNode].addMass(edgeMass / 2);
        }

        HashMap<SketchRenderMaterial, BuiltRenderLayer>[] mapArray = new HashMap[SketchFace.LAYER_COUNT];
        for(int i = 0; i < mapArray.length; i++) {
            mapArray[i] = new HashMap<>();
        }

        for(int i = 0; i < this.faces.size(); i++) {
            SketchFace face = this.faces.get(i);
            face.id = i;
            faces[i] = new BuiltFace(face);

            double faceMass = face.getMass();
            for(int j : faces[i].nodes) {
                nodes[j].addMass(faceMass / faces[i].nodes.length);
            }

            for(int j = 0; j < face.renderMaterial.length; j++) {
                SketchRenderMaterial rm = face.renderMaterial[j];
                if(rm != null) {
                    if(j < mapArray.length) {
                        HashMap<SketchRenderMaterial, BuiltRenderLayer> map = mapArray[j];
                        BuiltRenderLayer brl;
                        if(map.containsKey(rm)) {
                            brl = map.get(rm);
                        } else {
                            brl = new BuiltRenderLayer(rm);
                            map.put(rm ,brl);
                        }
                        brl.accept(face, faces[i], j);
                    }
                }
            }
        }

        for(int i = 0; i < this.transforms.size(); i++) {
            this.transforms.get(i).id = i;
            transforms[i] = new BuiltRigidBody(this.transforms.get(i));
        }

        int i = 0;
        for(HashMap<SketchRenderMaterial, BuiltRenderLayer> h : mapArray) {
            i += h.size();
        }
        BuiltRenderLayer[] layers = new BuiltRenderLayer[i];
        i = 0;
        for(HashMap<SketchRenderMaterial, BuiltRenderLayer> h : mapArray) {
            for(BuiltRenderLayer brl : h.values()) {
                if(i < layers.length) {
                    layers[i] = brl;
                    i++;
                } else {
                    break;
                }
            }
        }

        return new BuiltWeave(nodes, edges, faces, transforms, new BuiltSettings(this.getSettings()), layers);
    }

    public void setIds() {
        for(int i = 0; i < this.physicsMaterials.size(); i++) {
            this.physicsMaterials.get(i).id = i;
        }
        for(int i = 0; i < this.renderMaterials.size(); i++) {
            this.renderMaterials.get(i).id = i;
        }

        for(int i = 0; i < this.transforms.size(); i++) {
            this.transforms.get(i).id = i;
        }
        for(int i = 0; i < this.vertexCollections.size(); i++) {
            this.vertexCollections.get(i).id = i + this.transforms.size();
        }

        int j = 0;
        for(int i = 0; i < this.vertexCollections.size(); i++) {
            for(SketchVertex v : this.vertexCollections.get(i).getVertices()) {
                v.id = j;
                j++;
            }
        }
        for(SketchVertex v : this.nodes) {
            if(v.parent == null) {
                v.id = j;
                j++;
            }
        }

        for(int i = 0; i < this.edges.size(); i++) {
            this.edges.get(i).id = i;
        }

        for(int i = 0; i < this.faces.size(); i++) {
            this.faces.get(i).id = i;
        }
    }

    public void markAllAsAdded() {
        for(SketchElement e : this.nodes) {
            e.add();
        }
        for(SketchElement e : this.edges) {
            e.add();
        }
        for(SketchElement e : this.faces) {
            e.add();
        }
        for(SketchElement e : this.transforms) {
            e.add();
        }
        for(SketchElement e : this.vertexCollections) {
            e.add();
        }
        for(SketchElement e : this.physicsMaterials) {
            e.add();
        }
        for(SketchElement e : this.renderMaterials) {
            e.add();
        }
    }

    public SketchElement getObject(int i) {
        if(i < 0) return null;
        if(i < transforms.size()) return transforms.get(i);
        i -= transforms.size();
        if(i < vertexCollections.size()) return vertexCollections.get(i);
        return null;
    }

    public void setRoot() {
        if(this.transforms.isEmpty()) return;
        this.root = this.transforms.get(0);
        this.root.setDeletable(false);
    }
}
