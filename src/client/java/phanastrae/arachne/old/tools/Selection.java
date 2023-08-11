package phanastrae.arachne.old.tools;

import phanastrae.old.Face;
import phanastrae.old.Node;
import phanastrae.old.link_type.Link;

import java.util.ArrayList;
import java.util.List;

public class Selection {
    List<Node> nodes = new ArrayList<>();
    List<Link> edges = new ArrayList<>();
    List<Face> faces = new ArrayList<>();

    public void addNode(Node node) {
        if(node != null) {
            this.nodes.add(node);
        }
    }

    public void addEdge(Link edge) {
        if(edge != null) {
            this.edges.add(edge);
        }
    }

    public void addFace(Face face) {
        if(face != null) {
            this.faces.add(face);
        }
    }

    public void clear() {
        this.nodes.clear();
        this.edges.clear();
        this.faces.clear();
    }

    public void addSelection(Selection selection) {
        this.nodes.addAll(selection.nodes);
        this.edges.addAll(selection.edges);
        this.faces.addAll(selection.faces);
    }

    public List<Node> getNodes() {
        return this.nodes;
    }

    public List<Link> getEdges() {
        return this.edges;
    }

    public List<Face> getFaces() {
        return this.faces;
    }

    public boolean contains(Node node) {
        return this.nodes.contains(node);
    }

    public boolean contains(Link edge) {
        return this.edges.contains(edge);
    }

    public boolean contains(Face face) {
        return this.faces.contains(face);
    }
}
