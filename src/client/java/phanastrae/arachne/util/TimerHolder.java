package phanastrae.arachne.util;

import net.minecraft.client.MinecraftClient;
import phanastrae.arachne.Arachne;

import java.util.*;

public class TimerHolder {

    // TODO: There may be existing vanilla code that does everything here, if so consider switching to it
    // TODO: also this whole system might be a bit of a mess

    private static final TimerHolder INSTANCE = new TimerHolder();

    private final TimerTreeNode ROOT = new TimerTreeNode("root", 0);
    private final ArrayList<TimerTreeNode> path;

    public TimerHolder() {
        this.path = new ArrayList<>();
        goToRoot();
    }

    public static TimerHolder getInstance() {
        return INSTANCE;
    }

    // gets timers in a nice order e.g. node1, node1.child1, node1.child2, node2, node2.child1, node2.child1.child1, node2.child1.child2, node3, etc.
    // TODO: cache result
    // TODO: is there an existing implementation i can just use instead?
    public ArrayList<TimerTreeNode> getTimerTreeNodes() {
        // want to step through all existing nodes in a nice order
        ArrayList<TimerTreeNode> timers = new ArrayList<>();
        // start by going to root
        ArrayList<TimerTreeNode> path = new ArrayList<>();
        ArrayList<Integer> visitedChildren = new ArrayList<>();
        path.add(this.ROOT);
        visitedChildren.add(0);
        // add root to list
        timers.add(this.ROOT);
        // repeat following until all root's children traversed
        // TODO: add another safety limit?
        while(!path.isEmpty()) {
            TimerTreeNode node = path.get(path.size() - 1);
            ArrayList<TimerTreeNode> children = node.getChildrenSorted();
            // if node has unvisited children, go to next unvisited child, add child to list
            int next = visitedChildren.get(visitedChildren.size()-1);
            if(next < children.size()) {
                // mark child visited, queue next child
                visitedChildren.set(visitedChildren.size()-1, next + 1);
                // go to child
                TimerTreeNode child = children.get(next);
                path.add(child);
                visitedChildren.add(0);
                timers.add(child);
            } else {
                // all children visited, step up
                path.remove(path.size()-1);
                visitedChildren.remove(visitedChildren.size()-1);
            }
        }
        return timers;
    }

    public ArrayList<Timer> getTimers() {
        // TODO: DELETE
        return null;
    }

    public void tickAllTimers() {
        goToRoot();
        for(TimerTreeNode timer : getTimerTreeNodes()) {
            timer.getTimer().tick();
        }
    }

    public void push(String string) {
        if(this.path.isEmpty()) {
            goToRoot();
        }

        TimerTreeNode current = this.path.get(this.path.size()-1);
        TimerTreeNode child = current.getChild(string);
        this.path.add(child);
        child.getTimer().enable();
    }

    public void pop() {
        if(this.path.isEmpty()) {
            goToRoot();
            return;
        }

        TimerTreeNode current = this.path.get(this.path.size()-1);
        current.getTimer().disable();
        this.path.remove(this.path.size()-1);
    }

    public static void dualPush(String string) {
        MinecraftClient.getInstance().getProfiler().push(string);
        getInstance().push(string);
    }

    public static void dualPop() {
        MinecraftClient.getInstance().getProfiler().pop();
        getInstance().pop();
    }

    private void goToRoot() {
        this.path.clear();
        this.path.add(ROOT);
    }
}
