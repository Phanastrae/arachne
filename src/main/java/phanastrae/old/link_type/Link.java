package phanastrae.old.link_type;

import phanastrae.old.Node;

public abstract class Link {
    public Node node1;
    public Node node2;

    public Link(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public void tickLink(double dt) {
    }
}
