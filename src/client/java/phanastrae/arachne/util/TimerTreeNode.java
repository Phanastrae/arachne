package phanastrae.arachne.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TimerTreeNode implements Comparable {

    private final String name;
    private final int depth;
    private final Timer timer;
    private final HashMap<String, TimerTreeNode> children = new HashMap<String, TimerTreeNode>();

    public TimerTreeNode(String name, int depth) {
        this.name = name;
        this.depth = depth;
        this.timer = new Timer();
    }

    public String getName() {
        return this.name;
    }

    public int getDepth() {
        return this.depth;
    }

    public Timer getTimer() {
        return this.timer;
    }

    public Collection<TimerTreeNode> getChildren() {
        return this.children.values();
    }

    public ArrayList<TimerTreeNode> getChildrenSorted() {
        ArrayList<TimerTreeNode> children = new ArrayList<>();
        this.children.values().stream().sorted().forEach(children::add);
        return children;
    }

    public TimerTreeNode getChild(String name) {
        if(this.children.containsKey(name)) {
            return this.children.get(name);
        } else {
            TimerTreeNode ttn = new TimerTreeNode(name, depth + 1);
            this.children.put(name, ttn);
            return ttn;
        }
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if(o instanceof TimerTreeNode ttn) {
            return this.name.compareTo(ttn.name);
        } else {
            return 1;
        }
    }
}
